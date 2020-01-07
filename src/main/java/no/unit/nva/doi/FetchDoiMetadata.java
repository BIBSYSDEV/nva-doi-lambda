package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, GatewayResponse> {

    //    private static final Logger logger = LoggerFactory.getLogger(FetchDoiMetadata.class);

    public static final String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String CORS_ORIGIN_HEADER_HOSTS = "http://localhost:3000";

    public static final String URL_IS_NULL = "The input parameter 'url' is null";
    public static final String ERROR_KEY = "error";

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
        LambdaLogger logger = null;

        if (context != null && context.getLogger() != null) {
            logger = context.getLogger();
        }


        String url = null;
        if (input != null && input.containsKey("queryStringParameters")) {
            Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
            url = queryStringParameters.get("url");
        }

        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        headers.put(CORS_ALLOW_ORIGIN_HEADER, CORS_ORIGIN_HEADER_HOSTS);

        String json;

        Response.Status statusCode;
        if (isNull(url)) {
            statusCode = Response.Status.BAD_REQUEST;
            json = getErrorAsJson(URL_IS_NULL);
        } else {
            try {
                String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.displayName());
                final String uri = new URI(decodedUrl).toURL().toString();
                json = this.getDoiMetadataInJson(uri);
                statusCode = Response.Status.OK;
            } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
                if (nonNull(logger)) {
                    logger.log(e.toString());
                }
                statusCode = Response.Status.BAD_REQUEST;
                json = getErrorAsJson(e.getMessage());
            } catch (IOException e) {
                if (nonNull(logger)) {
                    logger.log(e.getMessage());
                }
                statusCode = Response.Status.SERVICE_UNAVAILABLE;
                json = getErrorAsJson(e.getMessage());
            }
        }
        if (nonNull(logger)) {
            logger.log("json: " + json + ", statusCode:" + statusCode.getStatusCode());
        }
        return new GatewayResponse(json, headers, statusCode.getStatusCode());
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
