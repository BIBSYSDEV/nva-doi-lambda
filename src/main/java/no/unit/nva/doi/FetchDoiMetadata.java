package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class FetchDoiMetadata implements RequestHandler<Map<String, Object>, SimpleResponse> {

//    private static final Logger logger = LoggerFactory.getLogger(FetchDoiMetadata.class);

    public static final String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final String URL_IS_NULL = "The input parameter 'url' is null";
    public static final String ERROR_KEY = "error";
    /** Connection object handling the direct communication via http for (mock)-testing to be injected */
    protected transient DataciteConnection dataciteConnection;
    private LambdaLogger logger;

    public FetchDoiMetadata() {
        dataciteConnection = new DataciteConnection();
    }

    public FetchDoiMetadata(DataciteConnection dataciteConnection) {
        this.dataciteConnection = dataciteConnection;
    }

    @Override
    public SimpleResponse handleRequest(Map<String, Object> input, Context context) {
        logger = context.getLogger();
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
        String url = (String) queryStringParameters.get("url");
        logger.log("Incoming url:" + url+"\n");
//        Map<String, String> headers = new ConcurrentHashMap<>();
//        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
//        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        String json;
        int statusCode;
        if (url == null) {
            statusCode = Response.Status.BAD_REQUEST.getStatusCode();
            json = getErrorAsJson(URL_IS_NULL);
        } else {
            try {
                String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.displayName());
                final String uri = new URI(decodedUrl).toURL().toString();
                json = this.getDoiMetadataInJson(uri);
                statusCode = Response.Status.OK.getStatusCode();
            } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
                logger.log(e.toString());
                statusCode = Response.Status.BAD_REQUEST.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            } catch (IOException e) {
                logger.log(e.getMessage());
                statusCode = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            }
        }
        logger.log("json: "+json+", statusCode:"+statusCode);
        return new SimpleResponse(json, ""+statusCode);
    }

    /**
     * The method takes a doi-url as String and returns the corresponding metadata of a publication
     * as a json-formatted String.
     *
     * @param doi String representing a doi-url
     * @return a json containing metadata to the publication given by its doi
     * @throws IOException if connection fails
     * @throws URISyntaxException if the URI has wrong syntax
     */
    protected String getDoiMetadataInJson(String doi) throws URISyntaxException, IOException {
        System.out.println("getDoiMetadataInJson(doi:"+doi+")");

        final String doiPath = new URI(doi).getPath();
        String json = dataciteConnection.connect(doiPath);
        System.out.println("json:"+json);
        return json;
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
