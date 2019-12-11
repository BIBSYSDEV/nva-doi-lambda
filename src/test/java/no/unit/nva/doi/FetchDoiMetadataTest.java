package no.unit.nva.doi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchDoiMetadataTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DataciteConnection mockDataciteConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void successfulResponse() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        String url = "https://doi.org/10.1093/afraf/ady029";
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(url, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
    }

    @Test
    public void testIncorrectSchemeUrl() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        String url = "htps://doi.org/10.1093/afraf/ady029";
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(url, null);
        assertEquals(Response.Status.BAD_REQUEST, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String expectedUnkownProtocolErrorText = "unknown protocol: htps";
        assertEquals(fetchDoiMetadata.getErrorAsJson(expectedUnkownProtocolErrorText), content);
    }

    @Test
    public void testUrlIsNull() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(null, null);
        assertEquals(Response.Status.BAD_REQUEST, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(fetchDoiMetadata.getErrorAsJson(fetchDoiMetadata.URL_IS_NULL), content);
    }

    @Test
    public void test_extract_metadata_from_resource_content() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        String doi = "https://doi.org/10.1007/s40518-018-0111-y";
        String doiMetadataJson = fetchDoiMetadata.getDoiMetadataInJson(doi);
        assertNotNull(doiMetadataJson);
    }

    @Test(expected = URISyntaxException.class)
    public void test_getDoiMetadata_json_url_no_uri() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        String url = "https://doi.org/lets^Go^Wild";
        fetchDoiMetadata.getDoiMetadataInJson(url);
        fail();
    }

}
