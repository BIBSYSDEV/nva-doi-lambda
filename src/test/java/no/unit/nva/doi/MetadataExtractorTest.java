package no.unit.nva.doi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.gson.Gson;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class MetadataExtractorTest {

    private ListAppender<ILoggingEvent> listAppender;
    MetadataExtractor metadataExtractor;

    @Before
    public void before() {
        metadataExtractor = new MetadataExtractor((int) Duration.ofSeconds(30).toMillis());
        Logger logger = (Logger) LoggerFactory.getLogger(MetadataExtractor.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    public void test_extract_metadata_from_resource_content() throws IOException {
        String doi_articles_1 = readTestResourceFile("/doi_article_1.json");
        HashMap mock_response = new Gson().fromJson(doi_articles_1, HashMap.class);
        assertEquals(mock_response,
                metadataExtractor.extract_metadata_from_resource_content("https://doi.org/10.1007/s40518-018-0111-y").getMap());
    }

    @Test
    public void test_extract_metadata_from_resource_content_not_http() throws IOException {
        assertTrue("We send in an URL not starting with http or https, that should return an empty map",
                metadataExtractor.extract_metadata_from_resource_content("htps://doi.org/10.1007/s40518-018-0111-y").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_extract_metadata_from_url_no_doi() throws IOException {
        String url = "http://www.unit.no";
        MetadataMap metadataMap = metadataExtractor.extract_metadata_from_url(url);
        assertTrue(metadataMap.isEmpty());
    }

    @Test
    public void test_extract_metadata_from_url_redirecting_doi() throws IOException {
        String url = "https://doi.org/10.1126/science.169.3946.635";
        MetadataMap metadataMap = metadataExtractor.extract_metadata_from_url(url);
        assertFalse(metadataMap.isEmpty());
        assertEquals(url, metadataMap.getFirst("dc.identifier.doi"));
    }

    @Test
    public void test_followForwardingDoiIdentifier() throws IOException {
        String url = "https://doi.org/10.6084/m9.figshare.c.3808081.v1";
        MetadataMap metadataMap = metadataExtractor.extract_metadata_from_url(url);
        metadataExtractor.followForwardingDoiIdentifier(url, metadataMap);
        assertEquals(url, metadataMap.getFirst("dc.identifier.doi"));
    }

    @Test
    public void test_followForwardingDoiIdentifier_url_not_mapping_DcIdentifierDoi() throws IOException {
        String url = "https://doi.org/10.1080/0886571X.2018.1455557";
        MetadataMap metadataMap = metadataExtractor.extract_metadata_from_url(url);
        String fake_url = "faking_url";
        metadataExtractor.followForwardingDoiIdentifier(fake_url, metadataMap);
        assertEquals(url, metadataMap.getFirst("dc.identifier.doi"));
    }

    @Test
    public void test_meta() throws IOException {
//        String url = "https://doi.org/10.1126/science.169.3946.635";
//        String url = "https://doi.org/10.1126/science.aau2582";
        String url = "https://doi.org/10.1093/afraf/ady029";
        MetadataMap metaMap = metadataExtractor.meta(url);
        assertFalse("We should have got a not empty metaMap", metaMap.isEmpty());
        assertThat(url, CoreMatchers.containsString(metaMap.getFirst("citation_doi")));
    }

    @Test(expected = IOException.class)
    public void test_extract_metadata_from_url_no_uri() throws IOException {
        String url = "https://doi.org/lets^Go^Wild";
        metadataExtractor.extract_metadata_from_url(url);
    }

    @Test(expected = IOException.class)
    public void test_getDoiMetadata_json_url_no_uri() throws IOException {
        String url = "https://doi.org/lets^Go^Wild";
        metadataExtractor.getDoiMetadata_json(url);
    }

    @Test(expected = IOException.class)
    public void test_extract_metadata_dc_and_og_default_url_no_uri() throws IOException {
        String url = "https://doi.org/lets^Go^Wild";
        final Document doc = Jsoup.connect(url).timeout(metadataExtractor.getExternal_service_timeout()).get();
        metadataExtractor.extract_metadata_dc_and_og_default(doc);
    }

    @Test
    public void test_extract_metadata_dc_doi_url_no_uri() {
        String url = "https://doi.org/lets^Go^Wild";
        metadataExtractor.extract_metadata_dc_doi(url);
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertThat(logsList.get(0).getMessage(), CoreMatchers.containsString(url));
    }


    private String readTestResourceFile(String testFileName) throws NullPointerException {
        InputStream inputStream = MetadataExtractorTest.class.getResourceAsStream(testFileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

}
