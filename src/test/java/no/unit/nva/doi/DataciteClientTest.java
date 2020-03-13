package no.unit.nva.doi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import no.bibsys.aws.tools.IoUtils;
import no.unit.nva.utils.MockHttpClient;
import org.junit.Assert;
import org.junit.Test;

public class DataciteClientTest {

    public static final String EXAMPLE_URL = "http://example.org";
    public static final Path DATACITE_RESPONSE_FILE = Path.of("dataciteResponse.json");
    private static final String doiUrl = "https://doi.org/10.1007/s40518-018-0111-y";

    @Test
    public void testMockUrl()
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        DataciteClient dataciteClient = mock(DataciteClient.class);
        when(dataciteClient.createRequestUrl(anyString(), any(DataciteContentType.class)))
            .thenCallRealMethod();
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
            .thenCallRealMethod();
        when(dataciteClient.readStringFromUrl(any(URL.class))).thenReturn(new String());

        String metadata = dataciteClient
            .fetchMetadata(EXAMPLE_URL, DataciteContentType.CITEPROC_JSON);

        Assert.assertNotNull(metadata);
    }

    @Test
    public void testValidResponseUrl()
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        String expectedResponse = IoUtils.resourceAsString(DATACITE_RESPONSE_FILE);
        HttpClient mockHttpClient = createMockHttpClient(expectedResponse);
        DataciteClient dataciteClient = new DataciteClient(mockHttpClient);

        String stringFromUrl = dataciteClient.readStringFromUrl(URI.create(doiUrl).toURL());

        Assert.assertNotNull(stringFromUrl);
    }

    private HttpClient createMockHttpClient(String expectedResponse) throws IOException {
        return new MockHttpClient<String>(expectedResponse);
    }

    @Test
    public void testEmptyResponseUrl()
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        String emptyResponse = "";
        HttpClient mockHttpClient = createMockHttpClient(emptyResponse);
        DataciteClient dataciteClient = new DataciteClient(mockHttpClient);
        String stringFromUrl = dataciteClient.readStringFromUrl(URI.create(doiUrl).toURL());
        Assert.assertEquals(new String(), stringFromUrl);
    }

}
