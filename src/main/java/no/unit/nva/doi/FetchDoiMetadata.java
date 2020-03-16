package no.unit.nva.doi;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static no.unit.nva.doi.GatewayResponse.errorGatewayResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String INVALID_DOI_URL = "The property 'doi_url' is not a valid DOI";
    public static final String MISSING_ACCEPT_HEADER = "Missing Accept header";
    public static final Pattern DOI_URL_PATTERN =
        Pattern.compile("^https?://(dx\\.)?doi\\.org/(10(?:\\.[0-9]+)+)/(.+)$",
                        Pattern.CASE_INSENSITIVE);

    public static final Pattern DOI_STRING_PATTERN =
        Pattern.compile("^(doi:)?(10(?:\\.[0-9]+)+)/(.+)$", Pattern.CASE_INSENSITIVE);

    public static final String HEADERS = "headers";
    public static final String BODY = "body";

    /**
     * Connection object handling the direct communication via http for (mock)-testing to be
     * injected.
     */
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final transient DataciteClient dataciteClient;
    private transient CrossRefClient crossRefClient;
    private transient LambdaLogger logger;

    public FetchDoiMetadata() {
        this(new DataciteClient(), new CrossRefClient());
    }

    public FetchDoiMetadata(DataciteClient dataciteClient, CrossRefClient crossRefClient) {
        this.dataciteClient = dataciteClient;
        this.crossRefClient = crossRefClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {

        init(context);
        DoiLookup doiLookup;
        DataciteContentType dataciteContentType;

        try {
            Map<String, String> headers = (Map<String, String>) input.get(HEADERS);
            System.out.println(headers);
            dataciteContentType = DataciteContentType.lookup(
                Optional.ofNullable(headers.get(HttpHeaders.ACCEPT))
                        .orElseThrow(() -> new IllegalArgumentException(MISSING_ACCEPT_HEADER))
            );
            doiLookup = gson.fromJson((String) input.get(BODY), DoiLookup.class);
            validate(doiLookup);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return errorGatewayResponse(e.getMessage(), BAD_REQUEST.getStatusCode());
        }

        try {
            String doiMetadata = lookupDoiMetadata(doiLookup.getDoi(), dataciteContentType);
            return new GatewayResponse(doiMetadata, OK.getStatusCode(),
                                       dataciteContentType.getContentType());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.log(e.getMessage());
            return errorGatewayResponse(e.getMessage(), SERVICE_UNAVAILABLE.getStatusCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.log(e.getMessage());
            return errorGatewayResponse(e.getMessage(), INTERNAL_SERVER_ERROR.getStatusCode());

        }
    }

    private void init(Context context) {
        this.logger = context.getLogger();
        this.crossRefClient.setLogger(logger);
    }

    private String lookupDoiMetadata(String doiUrl, DataciteContentType dataciteContentType)
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        System.out.println("getDoiMetadata(doi:" + doiUrl + ")");
        Optional<String> crossRefResult = crossRefClient.fetchDataForDoi(doiUrl);
        if (crossRefResult.isEmpty()) {
            return dataciteClient.fetchMetadata(doiUrl, dataciteContentType);
        } else {
            return crossRefResult.get();
        }
    }

    private void validate(DoiLookup doiLookup) {
        if (!isValidDoi(doiLookup.getDoi())) {
            throw new IllegalStateException(INVALID_DOI_URL);
        }
    }

    private boolean isValidDoi(String doi) {
        Matcher urlMatcher = DOI_URL_PATTERN.matcher(doi);
        Matcher stringMatcher = DOI_STRING_PATTERN.matcher(doi);
        return urlMatcher.find() || stringMatcher.find();
    }

}