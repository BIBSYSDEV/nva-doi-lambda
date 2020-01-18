package no.unit.nva.doi;

import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DataciteConnectionUnitTest {

    public static final String MOCK_URL = "http://example.org/123";
    private static final String NONSENSE_DOI = "htp://www.example.org/:)";
    public static final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    @Test
    public void test_exists() {
        new DataciteConnection();
    }

    @Test
    public void test_getUrl() throws MalformedURLException {
        URL url = new URL(MOCK_URL);
        final DataciteConnection dataciteConnection = new DataciteConnection(url);
        assertEquals(url, dataciteConnection.getUrl());
    }

    @Test
    public void test_setUrl() throws MalformedURLException {
        final DataciteConnection dataciteConnection = new DataciteConnection();
        final URL url = new URL(MOCK_URL);
        dataciteConnection.setUrl(url);
        assertEquals(url, dataciteConnection.getUrl());
    }

    @Test
    public void test_setDataciteUrl() throws MalformedURLException {
        URL url = new URL(DATACITE_URL + MOCK_URL);
        final DataciteConnection dataciteConnection = new DataciteConnection();
        dataciteConnection.setDataciteURL(MOCK_URL);
        assertEquals(url, dataciteConnection.getUrl());
    }

    @Test
    public void test_connect_throws_IOException() {
        DataciteConnection dataciteConnection = new DataciteConnection();
        String expectedError = "No subject alternative DNS name matching data.datacite.org found.";
        try {
            dataciteConnection.connect(NONSENSE_DOI);
            fail();
        } catch (IOException exception) {
            assertEquals(expectedError, exception.getMessage());
        }
    }

}
