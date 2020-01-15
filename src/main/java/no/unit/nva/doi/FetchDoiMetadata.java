package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String URL_IS_NULL = "The input parameter 'url' is null";
    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String URL_KEY = "url";
    private static final String EMPTY_STRING = "";

    /**
     * Connection object handling the direct communication via http for (mock)-testing to be injected.
     */
    protected transient DataciteConnection dataciteConnection;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public FetchDoiMetadata() {
        dataciteConnection = new DataciteConnection();
    }

    public FetchDoiMetadata(DataciteConnection dataciteConnection) {
        this.dataciteConnection = dataciteConnection;
    }


    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {

        GatewayResponse gatewayResponse = new GatewayResponse();

        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }

        Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
        String url = queryStringParameters.get(URL_KEY);

        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.displayName());
            final String uri = new URI(decodedUrl).toURL().toString();
            gatewayResponse.setBody(this.getDoiMetadataInJson(uri));
            gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setErrorBody(e.getMessage());
            System.out.println(e.getMessage());
        } catch (IOException e) {
            gatewayResponse.setStatusCode(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            gatewayResponse.setErrorBody(e.getMessage());
            System.out.println(e.getMessage());
        }
        return gatewayResponse;
    }

    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        Map<String, String> queryStringParameters = Optional.ofNullable((Map<String, String>) input
                .get(QUERY_STRING_PARAMETERS_KEY)).orElse(new ConcurrentHashMap<>());
        String url = queryStringParameters.getOrDefault(URL_KEY, EMPTY_STRING);
        if (url.isEmpty()) {
            throw new RuntimeException(URL_IS_NULL);
        }
    }

    /**
     * The method takes a doi-url as String and returns the corresponding metadata of a publication
     * as a json-formatted String.
     *
     * @param doi String representing a doi-url
     * @return a json containing metadata to the publication given by its doi
     * @throws IOException        if connection fails
     * @throws URISyntaxException if the URI has wrong syntax
     */
    protected String getDoiMetadataInJson(String doi) throws URISyntaxException, IOException {
        System.out.println("getDoiMetadataInJson(doi:" + doi + ")");

        final String doiPath = new URI(doi).getPath();
        String jsonString = dataciteConnection.connect(doiPath);
        JsonObject jsonObject = GSON.fromJson(jsonString, JsonObject.class);
        return GSON.toJson(jsonObject);
    }

}