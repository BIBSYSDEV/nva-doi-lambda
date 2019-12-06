package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.MalformedParametersException;
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
public class FetchDoiMetadata implements RequestHandler<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(FetchDoiMetadata.class);

    /** Defines how long we wait for datacite to answer */
    private transient int external_service_timeout = 30_000;
    /** datacite's URL to access json-formatted metadata  */
    private final static transient String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    @Override
    public Object handleRequest(String url, Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        String json;
        int statusCode;
        try {
            final String uri = getValidURI(url, 1024);
            json = getDoiMetadataInJson(uri);
            statusCode= 200;
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            logger.warn(e.getMessage(), e);
            statusCode = 400;
            json = "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            statusCode= 503;
            json = "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return new GatewayResponse(json, headers, statusCode);
    }

    /**
     * Helper method to check on url constraints as maxLength, emptiness, etc. returning the url UTF-8 encoded.
     * @param url url as String
     * @param maxLength maximum length of the url (default=1024)
     * @return UTF-8 encoded url
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    protected String getValidURI(String url, int maxLength) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        if (url == null || url.isEmpty()) {
            throw new MalformedParametersException("url=" + url);
        } else if (url.length() > maxLength ) {
            throw new MalformedParametersException("url.length > " + maxLength);
        }
        return new URI(URLDecoder.decode(url, StandardCharsets.UTF_8.displayName())).toURL().toString();
    }

    /**
     * The method takes a doi-url as String and returns the corresponding metadata of a publication
     * as a json-formatted String.
     * @param doi String representing a doi-url
     * @return a json containing metadata to the publication given by its doi
     * @throws IOException
     */
    protected String getDoiMetadataInJson(String doi) throws IOException {
        final String doiPath;
        try {
            doiPath = new URI(doi).getPath();
        } catch (URISyntaxException e) {
            logger.error("Error in url: " + doi, e);
            throw new IOException(e.getMessage() + " " + doi, e);
        }
        final String dataCite = DATACITE_URL + doiPath;
        final Connection connection = Jsoup.connect(dataCite).timeout(external_service_timeout).ignoreContentType(true);
        String json = connection.get().wholeText();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    /**
     * Set the timeout interval to wait for datacite to answer.
     * @param externalServiceTimeout
     */
    protected void setExternalServiceTimeout(int externalServiceTimeout) {
        external_service_timeout = externalServiceTimeout;
    }

}
