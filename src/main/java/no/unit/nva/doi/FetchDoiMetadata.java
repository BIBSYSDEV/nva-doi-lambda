package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String INVALID_DOI_URL = "The property 'doi_url' is not a valid DOI";
    public static final String MISSING_ACCEPT_HEADER = "Missing Accept header";
    private static final Pattern DOI_URL_PATTERN = Pattern.compile("^https?://(dx\\.)?doi\\.org/"
            + "(10(?:\\.[0-9]+)+)/(.+)$", Pattern.CASE_INSENSITIVE);

    /**
     * Connection object handling the direct communication via http for (mock)-testing to be injected.
     */
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final transient DataciteClient dataciteClient;

    public FetchDoiMetadata() {
        this(new DataciteClient());
    }

    public FetchDoiMetadata(DataciteClient dataciteClient) {
        this.dataciteClient = dataciteClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {

        GatewayResponse gatewayResponse = new GatewayResponse();
        DoiLookup doiLookup;
        DataciteContentType dataciteContentType;

        try {
            Map<String,String> headers = (Map<String, String>) input.get("headers");
            System.out.println(headers);
            dataciteContentType = DataciteContentType.lookup(
                    Optional.ofNullable(headers.get(HttpHeaders.ACCEPT))
                    .orElseThrow(() -> new IllegalArgumentException(MISSING_ACCEPT_HEADER))
            );
            doiLookup = gson.fromJson((String) input.get("body"), DoiLookup.class);
            validate(doiLookup);
        } catch (Exception e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }

        try {
            String doiMetadata = lookupDoiMetadata(doiLookup.getDoiUrl(), dataciteContentType);
            gatewayResponse.setBody(doiMetadata);
            gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
        } catch (IOException e) {
            gatewayResponse.setStatusCode(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            gatewayResponse.setErrorBody(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            gatewayResponse.setErrorBody(e.getMessage());
            System.out.println(e.getMessage());
        }
        return gatewayResponse;
    }

    private String lookupDoiMetadata(URL doiUrl, DataciteContentType dataciteContentType) throws IOException {
        System.out.println("getDoiMetadata(doi:" + doiUrl + ")");
        return dataciteClient.fetchMetadata(doiUrl.toString(), dataciteContentType);
    }

    private void validate(DoiLookup doiLookup) {
        if (!isValidDoi(doiLookup.getDoiUrl())) {
            throw new IllegalStateException(INVALID_DOI_URL);
        }
    }

    private boolean isValidDoi(URL url) {
        Matcher m = DOI_URL_PATTERN.matcher(url.toString());
        return m.find();
    }

}