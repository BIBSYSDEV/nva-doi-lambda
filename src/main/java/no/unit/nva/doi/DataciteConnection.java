package no.unit.nva.doi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class DataciteConnection {

    /**
     * Datacite's URL to access json-formatted metadata.
     */
    private static final String DATACITE_URL
            = "https://data.datacite.org/application/vnd.citationstyles.csl+json";
    protected URL url;

    public DataciteConnection() {
    }

    public DataciteConnection(URL url) {
        this.url = url;
    }

    protected void createUrl(String doiPath) throws MalformedURLException {
        String url = DATACITE_URL + doiPath;
        this.url =  new URL(url);
    }

    /**
     * Connect to datacite with given doi-url and returning the response as String.
     *
     * @param doiPath doi
     * @return json-response as String
     * @throws IOException something went wrong while connecting
     */
    protected String connect(String doiPath) throws IOException {
        String contents;
        createUrl(doiPath);
        URLConnection urlConnection = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            contents = in.lines().collect(Collectors.joining());
        }
        return contents;
    }

}
