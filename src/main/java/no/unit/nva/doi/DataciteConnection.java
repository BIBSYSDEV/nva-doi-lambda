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
    public static final String MALFORMED_URL_ERROR_TEMPLATE = "The url %s was malformed";
    public static final String UNREACHBLE_URL_ERROR_TEMPLATE = "The URL %s was unreachable";

    protected URL createUrl(String doiPath) throws DataciteConnectionError {
        String url = DATACITE_URL + doiPath;
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new DataciteConnectionError(String.format(MALFORMED_URL_ERROR_TEMPLATE, url));
        }
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
        URL url = createUrl(doiPath);
        InputStreamReader inputStreamReader = this.communicateWith(url);

        try (BufferedReader in = new BufferedReader(inputStreamReader)) {
            contents = in.lines().collect(Collectors.joining());
        }
        return contents;
    }

    /**
     * Make actual the contact with the url.
     *
     * @param url destination
     * @return InputStreamReader carrying the response
     * @throws IOException something went wrong in communication
     */
    protected InputStreamReader communicateWith(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        try (InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream())) {
            return inputStreamReader;
        } catch (IOException exception) {
            throw new DataciteConnectionError(String.format(UNREACHBLE_URL_ERROR_TEMPLATE, url));
        }
    }
}
