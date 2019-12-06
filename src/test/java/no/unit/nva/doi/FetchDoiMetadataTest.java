package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.*;

public class FetchDoiMetadataTest {

    private transient FetchDoiMetadata fetchDoiMetadata;

    @Before
    public void setUp() {
        fetchDoiMetadata = new FetchDoiMetadata();
    }

    @Test
    public void successfulResponse() {
        String url = "https://doi.org/10.1093/afraf/ady029";
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(url, null);
        assertEquals(200, result.getStatusCode());
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(url));
        assertTrue(content.contains("The political economy of banking in Angola"));
    }

    @Test
    public void testBadRequestResponse() {
        String url = "htps://doi.org/10.1093/afraf/ady029";
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(url, null);
        assertEquals(400, result.getStatusCode());
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertEquals("{\"error\": \"unknown protocol: htps\"}", content);
    }

    @Test
    public void testServerTimeoutResponse() {
        fetchDoiMetadata.setExternalServiceTimeout(10);
        String url = "https://doi.org/10.1093/afraf/ady029";
        GatewayResponse result = (GatewayResponse) fetchDoiMetadata.handleRequest(url, null);
        assertEquals(503, result.getStatusCode());
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertEquals("{\"error\": \"connect timed out\"}", content);
    }

    @Test
    public void test_getValidURI_valid() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        String url = "https://doi.org/10.1093/afraf/ady029";
        assertEquals(url, fetchDoiMetadata.getValidURI(url, 1024));
    }

    @Test(expected = URISyntaxException.class)
    public void test_getValidURI_invalid() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        String url = "https:// doi.org/";
        fetchDoiMetadata.getValidURI(url, 1024);
        fail();
    }

    @Test(expected = MalformedParametersException.class)
    public void test_getValidURI_invalidLength() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        String url = "https://doi.org/10.1093/afraf/ady029";
        fetchDoiMetadata.getValidURI(url, 10);
        fail();
    }

    @Test(expected = MalformedParametersException.class)
    public void test_getValidURI_nullURL() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        fetchDoiMetadata.getValidURI(null, 1024);
        fail();
    }

    @Test
    public void test_extract_metadata_from_resource_content() throws IOException {
        String doi_articles_1 = readTestResourceFile("/doi_article_1.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String mock_response = gson.toJson(doi_articles_1, String.class);
        String doiMetadata_json = fetchDoiMetadata.getDoiMetadataInJson("https://doi.org/10.1007/s40518-018-0111-y");
        assertEquals(mock_response, doiMetadata_json);
    }

    @Test(expected = IOException.class)
    public void test_getDoiMetadata_json_url_no_uri() throws IOException {
        String url = "https://doi.org/lets^Go^Wild";
        fetchDoiMetadata.getDoiMetadataInJson(url);
        fail();
    }

    private String readTestResourceFile(String testFileName) throws NullPointerException {
        InputStream inputStream = FetchDoiMetadataTest.class.getResourceAsStream(testFileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }


}
