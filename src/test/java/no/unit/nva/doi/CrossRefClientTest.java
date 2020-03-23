package no.unit.nva.doi;

import static no.unit.nva.doi.CrossRefClient.WORKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Optional;
import no.bibsys.aws.tools.IoUtils;
import no.unit.nva.utils.AbstractLambdaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CrossRefClientTest extends AbstractLambdaTest {

    private CrossRefClient crossRefClient;

    @BeforeEach
    void before() throws IOException {
        HttpClient httpClient = mockHttpClientWithNonEmptyResponse();
        crossRefClient = new CrossRefClient(httpClient);
        crossRefClient.setLogger(logger);
    }

    @DisplayName("createTargetUrl returns a valid Url for DOI strings that are not DOI URLs")
    @Test
    public void createTargetUrlReturnsAValidUrlForDoiStringThatIsNotDoiURL()
        throws URISyntaxException {
        String expected = String.join("/", CrossRefClient.CROSSREF_LINK, WORKS, DoiString);

        String output = crossRefClient.createUrlToCrossRef(DoiString).toString();
        assertThat(output, is(equalTo(expected)));
    }

    @DisplayName("createTargetUrl returns a valid Url for DOI strings that are DOI DX URLs")
    @Test
    public void createTargetUrlReturnsAValidUrlForDoiStringThatIsDoiDxUrl()
        throws URISyntaxException {
        targetURlReturnsAValidUrlForDoiStrings(DoiDxUrlPrefix);
    }

    @DisplayName("createTargetUrl returns a valid Url for DOI strings that are DOI URLs")
    @Test
    public void createTargetUrlReturnsAValidUrlForDoiStringThatIsDoiURL()
        throws URISyntaxException {
        targetURlReturnsAValidUrlForDoiStrings(DoiUrlPrefix);
    }

    @Test
    @DisplayName("fetchDataForDoi returns an Optional with a json object for an existing URL")
    public void fetchDataForDoiReturnAnOptionalWithAJsonObjectForAnExistingUrl()
        throws IOException, URISyntaxException {
        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString).map(MetadataAndContentLocation::getJson);
        String expected = IoUtils.resourceAsString(CrossRefSamplePath);
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(equalTo(expected)));
    }

    @Test
    @DisplayName("fetchDataForDoi returns an empty Optional for a non existing URL")
    public void fetchDataForDoiReturnAnEmptyOptionalForANonExistingUrl()
        throws URISyntaxException {

        CrossRefClient crossRefClient = crossRefClientReceives404();

        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString).map(MetadataAndContentLocation::getJson);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("fetchDataForDoi returns an empty Optional for an unknown error")
    public void fetchDataForDoiReturnAnEmptyOptionalForAnUnknownError()
        throws URISyntaxException {
        CrossRefClient crossRefClient = crossRefClientReceives500();
        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString).map(MetadataAndContentLocation::getJson);
        assertTrue(result.isEmpty());
    }

    private void targetURlReturnsAValidUrlForDoiStrings(String doiPrefix)
        throws URISyntaxException {
        String doiURL = String.join("/", doiPrefix, DoiString);
        String expected = String.join("/", CrossRefClient.CROSSREF_LINK, WORKS, DoiString);

        String output = crossRefClient.createUrlToCrossRef(doiURL).toString();
        assertThat(output, is(equalTo(expected)));
    }
}
