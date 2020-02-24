package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.unit.nva.doi.DataciteContentType.CITEPROC_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class FetchDoiMetadataTest {

    public static final String VALID_DOI = "https://doi.org/10.1093/afraf/ady029";
    public static final String MOCK_ERROR_MESSAGE = "The test told me to fail";
    public static final String INVALID_DOI = "https://doi.org/lets^Go^Wild";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";
    public static final String ERROR = "error";
    public static final String ERROR_KEY = "error";
    public static final String DATACITE_RESPONSE = "";

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void successfulResponse() throws Exception {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class))).thenReturn(DATACITE_RESPONSE);
        Map<String,Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE),
                CITEPROC_JSON.getContentType());
        String content = result.getBody();
        assertNotNull(content);
    }

    private Map<String, Object> createEvent(String doiUrlString, DataciteContentType dataciteContentType)
            throws MalformedURLException {
        Map<String, Object> event = new HashMap<>();
        DoiLookup doiLookup = new DoiLookup();
        doiLookup.setDoiUrl(new URL(doiUrlString));
        event.put("body", gson.toJson(doiLookup));
        event.put("headers", Collections.singletonMap(HttpHeaders.ACCEPT, dataciteContentType.getContentType()));
        return event;
    }

    @Test
    public void testMissingAcceptHeader() throws IOException {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class))).thenReturn(DATACITE_RESPONSE);
        Map<String, Object> event = new HashMap<>();
        event.put("headers", Collections.emptyMap());

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(FetchDoiMetadata.MISSING_ACCEPT_HEADER), content);
    }


    @Test
    public void testInvalidDoiUrl() throws IOException {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class))).thenReturn(DATACITE_RESPONSE);
        Map<String,Object> event = createEvent(INVALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(FetchDoiMetadata.INVALID_DOI_URL), content);
    }

    @Test
    public void testCommunicationIssuesOnCallingHandler() throws Exception {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
                .thenThrow(new IOException(MOCK_ERROR_MESSAGE));
        Map<String,Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(MOCK_ERROR_MESSAGE), content);
    }

    @Test
    public void testUnexpectedException() throws Exception {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
                .thenThrow(new NullPointerException());
        Map<String,Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(null), content);
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
