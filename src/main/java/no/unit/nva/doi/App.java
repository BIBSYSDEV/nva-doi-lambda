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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    int external_service_timeout = 30000;
    final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";



    public Object handleRequest(String url, Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        String json;
        int statusCode;
        try {
            final String uri = getValidURI(url, 1024);
            json = getDoiMetadata_json(uri);
            statusCode= 200;
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            logger.warn(e.getMessage(), e);
            statusCode = 400;
            json = "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            statusCode= 503;
            json = "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            statusCode = 500;
            json = "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return new GatewayResponse(json, headers, statusCode);
    }

    String getValidURI(String url, int maxLength) throws MalformedParametersException, UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        if (url == null || url.isEmpty()) {
            throw new MalformedParametersException("url=" + url);
        } else if (url.length() > maxLength ) {
            throw new MalformedParametersException("url.length > " + maxLength);
        }
        return new URI(URLDecoder.decode(url, StandardCharsets.UTF_8.displayName())).toURL().toString();
    }

    String getDoiMetadata_json(String doi) throws IOException {
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

    void setExternalServiceTimeout(int externalServiceTimeout) {
        external_service_timeout = externalServiceTimeout;
    }

}
