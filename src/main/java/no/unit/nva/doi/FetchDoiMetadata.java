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

import static java.util.Objects.isNull;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String URL_IS_NULL = "The input parameter 'url' is null";
    public static final String ERROR_KEY = "error";
    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String URL_KEY = "url";

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
        String url = null;
        if (input != null && input.containsKey(QUERY_STRING_PARAMETERS_KEY)) {
            Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
            url = queryStringParameters.get(URL_KEY);
        }
        String json;
        Response.Status statusCode;
        if (isNull(url)) {
            statusCode = Response.Status.BAD_REQUEST;
            json = getErrorAsJson(URL_IS_NULL);
            return new GatewayResponse(json, statusCode.getStatusCode());
        }
        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.displayName());
            final String uri = new URI(decodedUrl).toURL().toString();
            json = this.getDoiMetadataInJson(uri);
            statusCode = Response.Status.OK;
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            statusCode = Response.Status.BAD_REQUEST;
            json = getErrorAsJson(e.getMessage());
            System.out.println(e.getMessage());
        } catch (IOException e) {
            statusCode = Response.Status.SERVICE_UNAVAILABLE;
            json = getErrorAsJson(e.getMessage());
            System.out.println(e.getMessage());
        }
        return new GatewayResponse(json, statusCode.getStatusCode());
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
        String json = dataciteConnection.connect(doiPath);
        return GSON.toJson(json);
    }

    /**
     * Get error message as a json string.
     *
     * @param message message from exception
     * @return String containing an error message as json
     */
    protected String getErrorAsJson(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        return json.toString();
    }

}