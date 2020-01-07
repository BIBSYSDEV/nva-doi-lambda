package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchDoiMetadataTest {

    public static final String VALID_DOI = "https://doi.org/10.1093/afraf/ady029";
    public static final String INVALID_URL_SCHEME = "htps://doi.org/10.1093/afraf/ady029";
    public static final String UNKNOWN_PROTOCOL_HTPS = "unknown protocol: htps";
    public static final String MOCK_ERROR_MESSAGE = "The test told me to fail";
    public static final String INVALID_DOI = "https://doi.org/lets^Go^Wild";
    public static final String DATACITE_RESPONSE_JSON = "/dataciteResponse.json";
    public static final String REGEX_MATCH_BEGINNING_OF_STRING = "\\A";
    public static final String EMPTY_STRING = "";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";
    public static final String ERROR = "error";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DataciteConnection mockDataciteConnection;
    public static final Gson GSON = new GsonBuilder().create();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void successfulResponse() throws Exception {
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        String url = "https://doi.org/10.1093/afraf/ady029";
        Map<String, Object> event = new HashMap<>();
        Map<String,String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("url", url);
        event.put("queryStringParameters", queryStringParameters);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());

        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
    }

    @Test
    public void testIncorrectSchemeUrl() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        Map<String, Object> event = new HashMap<>();
        Map<String,String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("url", INVALID_URL_SCHEME);
        event.put("queryStringParameters", queryStringParameters);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(fetchDoiMetadata.getErrorAsJson(UNKNOWN_PROTOCOL_HTPS), content);
    }


    @Test
    public void testUrlIsNull() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        GatewayResponse result = fetchDoiMetadata.handleRequest(null, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(fetchDoiMetadata.getErrorAsJson(FetchDoiMetadata.URL_IS_NULL), content);
    }

    @Test
    public void testCommunicationIssuesOnCallingHandler() throws Exception {
        String url = "https://doi.org/10.1093/afraf/ady029";
        Map<String, Object> event = new HashMap<>();
        Map<String,String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("url", url);
        event.put("queryStringParameters", queryStringParameters);
        String mockErrorMessage = "The test told me to fail";
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.communicateWith(any())).thenThrow(new IOException(mockErrorMessage));
        when(fetchDoiMetadata.handleRequest(event, null)).thenCallRealMethod();
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(fetchDoiMetadata.getErrorAsJson(MOCK_ERROR_MESSAGE), content);
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
        String errorJson = fetchDoiMetadata.getErrorAsJson(ERROR);
        assertEquals(ERROR_JSON, errorJson);

    }

    @Test
    public void testDataciteConnectionAndParsingResult() throws Exception {
        InputStream inputStream = FetchDoiMetadataTest.class.getResourceAsStream(DATACITE_RESPONSE_JSON);
        InputStreamReader responseStreamReader = new InputStreamReader(inputStream);
        when(mockDataciteConnection.communicateWith(any())).thenReturn(responseStreamReader);
        when(mockDataciteConnection.connect(anyString())).thenCallRealMethod();
        String response = mockDataciteConnection.connect(VALID_DOI);
        LinkedHashMap<String, Object> actualResponse = GSON.fromJson(response, (Type) LinkedHashMap.class);
        String mockResponse = this.readTestResourceFileAsString();
        LinkedHashMap<String, Object> expectedResponse = GSON.fromJson(mockResponse, (Type) LinkedHashMap.class);
        assertEquals(expectedResponse, actualResponse);
    }

    private String readTestResourceFileAsString() throws NullPointerException {
        InputStream inputStream = FetchDoiMetadataTest.class
                .getResourceAsStream(FetchDoiMetadataTest.DATACITE_RESPONSE_JSON);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter(REGEX_MATCH_BEGINNING_OF_STRING);
            return scanner.hasNext() ? scanner.next() : EMPTY_STRING;
        }
    }

}
