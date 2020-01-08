package no.unit.nva.doi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, DataciteConnection.class })
public class DataciteConnectionTest {

    public static final String MOCK_URL = "http://example.org/123";
    private static final String NONSENSE_DOI = "htp://www.example.org/:)";
    public static final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";
    public static final String MOCK_BODY = "Svenn";

    @Test
    public void test_exists() {
        new DataciteConnection();
    }

    @Test
    public void test_createUrl() throws MalformedURLException {
        URL url = new URL(DATACITE_URL + MOCK_URL);
        final DataciteConnection dataciteConnection = new DataciteConnection(url);
        dataciteConnection.createUrl(MOCK_URL);
        assertEquals(url, dataciteConnection.url);
    }

    @Test
    public void test_connect_throws_IOException() {
        DataciteConnection dataciteConnection = new DataciteConnection();
        String expectedError = DATACITE_URL + NONSENSE_DOI;
        try {
            dataciteConnection.connect(NONSENSE_DOI);
            fail();
        } catch (IOException exception) {
            assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void test_successful_connect() throws Exception {
        URLConnection conn = PowerMockito.mock(URLConnection.class);
        URL mockUrl = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(mockUrl);
        DataciteConnection dataciteConnection = new DataciteConnection(mockUrl);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(MOCK_BODY.getBytes());
        PowerMockito.when(mockUrl.openConnection()).thenReturn(conn);
        PowerMockito.when(conn.getInputStream()).thenReturn(inputStream);
        String content = dataciteConnection.connect(MOCK_URL);
        assertEquals(MOCK_BODY, content);
    }

}