package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
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

//    @Test
    public void successfulResponse() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        when(mockDataciteConnection.connect(anyString())).thenReturn(anyString());
        String url = "https://doi.org/10.1093/afraf/ady029";

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("queryStringParameters","{url=https://doi.org/10.1093/afraf/ady029}");
        SimpleResponse result = fetchDoiMetadata.handleRequest(event, null);
//        assertEquals(Response.Status.OK, result.getStatus());
//        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
//        String content = result.getBody();
        assertNotNull(result);
    }


//    @Test
    public void testUrlIsNull() {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        SimpleResponse result = fetchDoiMetadata.handleRequest(null, null);
        assertNotNull(result);
//        assertEquals(fetchDoiMetadata.getErrorAsJson(FetchDoiMetadata.URL_IS_NULL), content);
    }

//    @Test
    public void testCommunicationIssuesOnCallingHandler() throws Exception {
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(mockDataciteConnection);
        String url = "https://doi.org/10.1093/afraf/ady029";
        String mockErrorMessage = "The test told me to fail";
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("queryStringParameters","{url=https://doi.org/10.1093/afraf/ady029}");

        when(mockDataciteConnection.communicateWith(any())).thenThrow(new IOException(mockErrorMessage));
        when(fetchDoiMetadata.handleRequest(event, null)).thenCallRealMethod();

        SimpleResponse result = fetchDoiMetadata.handleRequest(event, null);
        assertNotNull(result);
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

    @Test
    public void testErrorResponse() {
        String expectedJson = "{\"error\":\"error\"}";
        // calling real constructor (no need to mock as this is not talking to the internet)
        // but helps code coverage
        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata();
        String errorJson = fetchDoiMetadata.getErrorAsJson("error");
        assertEquals(expectedJson, errorJson);

    }

    @Test
    public void testDataciteConnectionAndParsingResult() throws Exception {
        InputStream inputStream = FetchDoiMetadataTest.class.getResourceAsStream("/dataciteResponse.json");
        InputStreamReader responseStreamReader = new InputStreamReader(inputStream);
        when(mockDataciteConnection.communicateWith(any())).thenReturn(responseStreamReader);
        when(mockDataciteConnection.connect(anyString())).thenCallRealMethod();
        String doi = "https://doi.org/10.1007/s40518-018-0111-y";
        String response = mockDataciteConnection.connect(doi);
        Gson gson = new GsonBuilder().create();
        LinkedHashMap actualResponse = gson.fromJson(response, LinkedHashMap.class);
        String mockResponse = this.readTestResourceFileAsString("/dataciteResponse.json");
        LinkedHashMap expectedResponse = gson.fromJson(mockResponse, LinkedHashMap.class);
        assertEquals(expectedResponse, actualResponse);
    }

    private String readTestResourceFileAsString(String testFileName) throws NullPointerException {
        InputStream inputStream = FetchDoiMetadataTest.class.getResourceAsStream(testFileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

}
