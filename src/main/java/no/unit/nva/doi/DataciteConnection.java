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
            = "https://data.crosscite.org/application/vnd.citationstyles.csl+json";
    /**
     * The url the connection should be established to.
     */
    private URL url;

    public DataciteConnection() {
    }

    /**
     * As parameter send in the url the connection should be established to.
     * @param url url
     */
    public DataciteConnection(URL url) {
        this.url = url;
    }

    /**
     * Sets the url to connect to.
     * @param url url
     */
    protected void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Get the url the connection should be established to.
     */
    protected URL getUrl() {
        return this.url;
    }

    /**
     * Sets an url pointing to datacite and appends the parameter doiPath.
     * @param doiPath doi-url
     */
    protected void setDataciteURL(String doiPath) throws MalformedURLException {
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
        setDataciteURL(doiPath);
        URLConnection urlConnection = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            contents = in.lines().collect(Collectors.joining());
        }
        return contents;
    }
}
