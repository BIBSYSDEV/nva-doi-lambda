package no.unit.nva.doi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class DataciteConnection {

    /** Datacite's URL to access json-formatted metadata.  */
    private final transient String Datacite_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    protected String connect(String doiPath) throws IOException {
        StringBuilder contents = new StringBuilder();
        URL url = new URL(Datacite_URL + doiPath);
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            contents.append(inputLine);
        }
        in.close();
        return contents.toString();
    }

}
