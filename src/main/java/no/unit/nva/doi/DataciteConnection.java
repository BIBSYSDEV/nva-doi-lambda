package no.unit.nva.doi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class DataciteConnection {

    /** Datacite's URL to access json-formatted metadata.  */
    private final static transient String dataciteURL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    /**
     * Connect to datacite with given doi-url and returning the response as String.
     * @param doiPath doi
     * @return json-response as String
     * @throws IOException something went wrong while connecting
     */
    protected String connect(String doiPath) throws IOException {
        InputStreamReader inputStreamReader = null;
        BufferedReader in = null;
        String contents;
        try {
            URL url = new URL(dataciteURL + doiPath);
            inputStreamReader = this.communicateWith(url);
            in = new BufferedReader(inputStreamReader);
            contents = in.lines().collect(Collectors.joining());
        } finally {
            if (in != null) {
                in.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }
        return contents;
    }

    /**
     * Make actual the contact with the url.
     * @param url destination
     * @return InputStreamReader carrying the response
     * @throws IOException something went wrong in communication
     */
    protected InputStreamReader communicateWith(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        return new InputStreamReader(connection.getInputStream());
    }

}
