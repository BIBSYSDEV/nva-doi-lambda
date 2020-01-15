package no.unit.nva.doi;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchDoiMetadataTest {

    public static final String VALID_DOI = "https://doi.org/10.1093/afraf/ady029";
    public static final String INVALID_URL_SCHEME = "htps://doi.org/10.1093/afraf/ady029";
    public static final String UNKNOWN_PROTOCOL_HTPS = "unknown protocol: htps";
    public static final String MOCK_ERROR_MESSAGE = "The test told me to fail";
    public static final String INVALID_DOI = "https://doi.org/lets^Go^Wild";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";
    public static final String ERROR = "error";
    public static final String ERROR_KEY = "error";
    public static final String DOI_URL = "https://doi.org/10.1093/afraf/ady029";
    public static final String WRONG_QUERY_STRING_PARAMETERS_KEY = "wrongQueryStringParameters";
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
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put(FetchDoiMetadata.URL_KEY, DOI_URL);
        event.put(FetchDoiMetadata.QUERY_STRING_PARAMETERS_KEY, queryStringParameters);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());

        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
    }

    @Test
    public void testIncorrectSchemeUrl() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata();
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put(FetchDoiMetadata.URL_KEY, INVALID_URL_SCHEME);
        event.put(FetchDoiMetadata.QUERY_STRING_PARAMETERS_KEY, queryStringParameters);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(UNKNOWN_PROTOCOL_HTPS), content);
    }

    @Test
    public void testUrlIsNull() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata();
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryStringParameters = new HashMap<>();
        event.put(FetchDoiMetadata.QUERY_STRING_PARAMETERS_KEY, queryStringParameters);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(FetchDoiMetadata.URL_IS_NULL), content);
    }

    @Test
    public void testMissingQueryParamsNull() {
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put(FetchDoiMetadata.URL_KEY, DOI_URL);
        event.put(WRONG_QUERY_STRING_PARAMETERS_KEY, queryStringParameters);
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata();
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(FetchDoiMetadata.URL_IS_NULL), content);
    }

    @Test
    public void testCommunicationIssuesOnCallingHandler() throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put(FetchDoiMetadata.URL_KEY, DOI_URL);
        event.put(FetchDoiMetadata.QUERY_STRING_PARAMETERS_KEY, queryStringParameters);
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.connect(anyString())).thenThrow(new IOException(MOCK_ERROR_MESSAGE));
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(MOCK_ERROR_MESSAGE), content);
    }

    @Test
    public void test_extract_metadata_from_resource_content() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        String doiMetadataJson = fetchDoiMetadata.getDoiMetadataInJson(VALID_DOI);
        assertNotNull(doiMetadataJson);
    }

    @Test(expected = URISyntaxException.class)
    public void test_getDoiMetadata_json_url_no_uri() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        fetchDoiMetadata.getDoiMetadataInJson(INVALID_DOI);
        fail();
    }

    @Test
    public void testErrorResponse() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata();
        String errorJson = getErrorAsJson(ERROR);
        assertEquals(ERROR_JSON, errorJson);
    }

    /**
     * Get error message as a json string.
     *
     * @param message message from exception
     * @return String containing an error message as json
     */
    private String getErrorAsJson(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        return json.toString();
    }

}
