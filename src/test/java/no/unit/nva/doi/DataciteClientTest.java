package no.unit.nva.doi;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataciteClientTest {

    public static final String EXAMPLE_URL = "http://example.org";

    @Test
    public void testMockUrl() throws IOException {
        DataciteClient dataciteClient = mock(DataciteClient.class);
        when(dataciteClient.createRequestUrl(anyString(), any(DataciteContentType.class))).thenCallRealMethod();
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class))).thenCallRealMethod();
        when(dataciteClient.readStringFromUrl(any(URL.class))).thenReturn(new String());

        String metadata = dataciteClient.fetchMetadata(EXAMPLE_URL, DataciteContentType.CITEPROC_JSON);

        Assert.assertNotNull(metadata);
    }

    @Test
    public void testExampleUrl() throws IOException {
        DataciteClient dataciteClient = mock(DataciteClient.class);
        when(dataciteClient.readStringFromUrl(any(URL.class))).thenCallRealMethod();
        URL exampleUrl = new URL(EXAMPLE_URL);
        String stringFromUrl = dataciteClient.readStringFromUrl(exampleUrl);
        Assert.assertNotNull(stringFromUrl);
    }
}
