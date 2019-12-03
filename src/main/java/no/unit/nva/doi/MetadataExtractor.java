package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MetadataExtractor {

    private final Logger logger = LoggerFactory.getLogger(MetadataExtractor.class);

    private final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    private int external_service_timeout;

    public MetadataExtractor() {
        external_service_timeout = 30000;
    }

    public MetadataExtractor(int externalServiceTimeout) {
        external_service_timeout = externalServiceTimeout;
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

}
