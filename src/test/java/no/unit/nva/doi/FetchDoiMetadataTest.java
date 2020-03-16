package no.unit.nva.doi;

import static no.unit.nva.doi.DataciteContentType.CITEPROC_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FetchDoiMetadataTest {

    public static final String VALID_DOI = "https://doi.org/10.1093/afraf/ady029";
    public static final String MOCK_ERROR_MESSAGE = "The test told me to fail";
    public static final String INVALID_DOI = "https://doi.org/lets^Go^Wild";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";
    public static final String ERROR = "error";
    public static final String ERROR_KEY = "error";
    public static final String DATACITE_RESPONSE = "";

    public static final String VALID_DOI_WITH_DOI_PREFIX = "doi:10.123.4.5/124";
    public static final String VALID_DOI_WITHOUT_DOI_PREFIX = "10.123.4.5/124";

    private static final Context mockLambdaContext = createMockContext();
    private static final CrossRefClient crossRefClient = createCrossRefClient();

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void successfulResponse() throws Exception {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
            .thenReturn(DATACITE_RESPONSE);
        Map<String, Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient, crossRefClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, mockLambdaContext);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE),
                     CITEPROC_JSON.getContentType());
        String content = result.getBody();
        assertNotNull(content);
    }

    private Map<String, Object> createEvent(String doiUrlString,
                                            DataciteContentType dataciteContentType)
        throws MalformedURLException {
        Map<String, Object> event = new HashMap<>();
        DoiLookup doiLookup = new DoiLookup();
        doiLookup.setDoi(doiUrlString);
        event.put("body", gson.toJson(doiLookup));
        event.put("headers", Collections
            .singletonMap(HttpHeaders.ACCEPT, dataciteContentType.getContentType()));
        return event;
    }

    @Test
    public void testMissingAcceptHeader()
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
            .thenReturn(DATACITE_RESPONSE);
        Map<String, Object> event = new HashMap<>();
        event.put("headers", Collections.emptyMap());

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient, crossRefClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, mockLambdaContext);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(getErrorAsJson(FetchDoiMetadata.MISSING_ACCEPT_HEADER), content);
    }

    @Test
    public void testInvalidDoiUrl()
        throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        DataciteClient dataciteClient = Mockito.mock(DataciteClient.class);
        when(dataciteClient.fetchMetadata(anyString(), any(DataciteContentType.class)))
            .thenReturn(DATACITE_RESPONSE);
        Map<String, Object> event = createEvent(INVALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient, crossRefClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, mockLambdaContext);

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
        Map<String, Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient, crossRefClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, mockLambdaContext);

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
        Map<String, Object> event = createEvent(VALID_DOI, CITEPROC_JSON);

        FetchDoiMetadata fetchDoiMetadata = new FetchDoiMetadata(dataciteClient, crossRefClient);
        GatewayResponse result = fetchDoiMetadata.handleRequest(event, mockLambdaContext);

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

    @Test
    @DisplayName("DOI_STRING_PATTERN matches doi string starting with doi prefix")
    public void doiStringPatternMatchesDoiStringStartingWithDoiPrefix() {
        Pattern regex = FetchDoiMetadata.DOI_STRING_PATTERN;
        boolean matchingResult = regex.matcher(VALID_DOI_WITH_DOI_PREFIX).matches();
        assertThat(matchingResult, is(true));

    }

    @Test
    @DisplayName("DOI_STRING_PATTERN matches doi string starting without doi prefix")
    public void doiStringPatternMatchesDoiStringStartingWithoutDoiPrefix() {
        Pattern regex = FetchDoiMetadata.DOI_STRING_PATTERN;
        boolean matchingResult = regex.matcher(VALID_DOI_WITHOUT_DOI_PREFIX).matches();
        assertThat(matchingResult, is(true));
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

    private static LambdaLogger createMockLogger() {
        LambdaLogger logger = mock(LambdaLogger.class);
        return logger;
    }

    private static Context createMockContext() {
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(createMockLogger());
        return context;
    }

    private static final CrossRefClient createCrossRefClient() {
        CrossRefClient client = mock(CrossRefClient.class);
        try {
            when(client.fetchDataForDoi(anyString())).thenReturn(Optional.empty());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return client;
    }

}
