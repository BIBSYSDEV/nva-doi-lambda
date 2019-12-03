package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class MetadataExtractorTest {

    MetadataExtractor metadataExtractor;

    @Before
    public void before() {
        metadataExtractor = new MetadataExtractor((int) Duration.ofSeconds(30).toMillis());
    }

    @Test
    public void test_extract_metadata_from_resource_content() throws IOException {
        String doi_articles_1 = readTestResourceFile("/doi_article_1.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String mock_response = gson.toJson(doi_articles_1, String.class);
        String doiMetadata_json = metadataExtractor.getDoiMetadata_json("https://doi.org/10.1007/s40518-018-0111-y");
        assertEquals(mock_response, doiMetadata_json);
    }

    @Test(expected = IOException.class)
    public void test_getDoiMetadata_json_url_no_uri() throws IOException {
        String url = "https://doi.org/lets^Go^Wild";
        metadataExtractor.getDoiMetadata_json(url);
    }

    private String readTestResourceFile(String testFileName) throws NullPointerException {
        InputStream inputStream = MetadataExtractorTest.class.getResourceAsStream(testFileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

}
