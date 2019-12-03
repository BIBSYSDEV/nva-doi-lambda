package no.unit.nva.doi;

import java.io.*;
import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public Object handleRequest(String url, Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        String json = "{}";
        int statusCode;
        try {
            MetadataExtractor metadataExtractor = new MetadataExtractor();
            final String uri = getValidURI(url, 1024);
            json = metadataExtractor.getDoiMetadata_json(uri);
            statusCode= 200;
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            logger.warn(e.getMessage(), e);
            statusCode = 400;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            statusCode= 503;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            statusCode = 500;
        }
        return new GatewayResponse(json, headers, statusCode);
    }

    String getValidURI(String url, int maxLenght) throws MalformedParametersException, UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        if (url == null || url.isEmpty()) {
            throw new MalformedParametersException("url=" + url);
        } else if (url.length() > maxLenght ) {
            throw new MalformedParametersException("url.length > " + maxLenght);
        }
        return new URI(URLDecoder.decode(url, StandardCharsets.UTF_8.displayName())).toURL().toString();
    }

}
