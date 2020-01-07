package no.unit.nva.doi;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class DataciteConnectionTest {

    public static final String MOCK_URL = "http://example.org/123";
    private static final String FAKE_DATACITE_URL = "http://example.org/10000";
    private static final String NONSENSE_DOI = "htp://www.example.org/:)";
    private static final String DATACITE_RESPONSE_JSON = "dataciteResponse.json";
    public static final String PATH_SEPARATOR = "/";
    public static final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";
    public static final String ERROR_TEMPLATE = "The URL %s was unreachable";

    @Test
    public void test_exists() {
        new DataciteConnection();
    }

    @Test
    public void test_connect_throws_IoException() {
        DataciteConnection dataciteConnection = new DataciteConnection();

        String expectedError = String.format(ERROR_TEMPLATE, DATACITE_URL + NONSENSE_DOI);

        try {
            dataciteConnection.connect(NONSENSE_DOI);
            fail();
        } catch (IOException exception) {
            assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void test_connect() throws IOException {
        DataciteConnection dataciteConnection = new DataciteConnection();
        DataciteConnection spiedDataciteConnection = spy(dataciteConnection);
        URL mockUrl = new URL(MOCK_URL);
        doReturn(mockUrl).when(spiedDataciteConnection).createUrl(anyString());
        try (
                InputStream inputStream = Objects.requireNonNull(this.getClass().getClassLoader()
                        .getResourceAsStream(DATACITE_RESPONSE_JSON))
        ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            doReturn(inputStreamReader)
                    .when(spiedDataciteConnection).communicateWith(mockUrl);

            String data = spiedDataciteConnection.connect(FAKE_DATACITE_URL);
            assertNotNull(data);
            String originalDataFromFile = IOUtils.resourceToString(PATH_SEPARATOR + DATACITE_RESPONSE_JSON,
                    StandardCharsets.UTF_8);
            assertTrue(compareJson(originalDataFromFile, data));
        }
    }

    private boolean compareJson(String expected, String input) {
        Gson gson = new Gson();
        Object object1 = gson.fromJson(expected, Object.class);
        Object object2 = gson.fromJson(input, Object.class);

        return object1.equals(object2);
    }
}