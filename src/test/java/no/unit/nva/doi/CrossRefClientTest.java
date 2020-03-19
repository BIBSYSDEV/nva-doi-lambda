package no.unit.nva.doi;

import static no.unit.nva.doi.CrossRefClient.WORKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import no.bibsys.aws.tools.IoUtils;
import no.unit.nva.utils.AbstractLambdaTest;
import no.unit.nva.utils.HttpResponseStatus200;
import no.unit.nva.utils.HttpResponseStatus404;
import no.unit.nva.utils.HttpResponseStatus500;
import no.unit.nva.utils.MockHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CrossRefClientTest extends AbstractLambdaTest {

    public static final String DoiString = "10.1007/s00115-004-1822-4";
    public static final String DoiDxUrlPrefix = "https://dx.doi.org";
    public static final String DoiUrlPrefix = "https://doi.org";
    public static final Path SampleResponsePath = Paths.get("crossRefSample.json");
    public static final String ERROR_MESSAGE = "404 error message";

    private CrossRefClient crossRefClient;
    private LambdaLogger logger = mockLambdaLogger();

    @BeforeEach
    void before() throws IOException {
        HttpClient httpClient = mockHttpClientWithNonEmptyResponse();
        crossRefClient = new CrossRefClient(httpClient);
        crossRefClient.setLogger(logger);
    }

    private HttpClient mockHttpClientWithNonEmptyResponse() throws IOException {
        String responseBody = IoUtils.resourceAsString(SampleResponsePath);
        HttpResponseStatus200<String> response = new HttpResponseStatus200<>(responseBody);
        return new MockHttpClient<>(response);
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
        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString);
        String expected = IoUtils.resourceAsString(SampleResponsePath);
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(equalTo(expected)));
    }

    @Test
    @DisplayName("fetchDataForDoi returns an empty Optional for a non existing URL")
    public void fetchDataForDoiReturnAnEmptyOptionalForANonExistingUrl()
        throws URISyntaxException {

        CrossRefClient crossRefClient = crossRefClientReceives404();

        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("fetchDataForDoi returns an empty Optional for an unknown error")
    public void fetchDataForDoiReturnAnEmptyOptionalForAnUnknownError()
        throws URISyntaxException {
        CrossRefClient crossRefClient = crossRefClientReceives500();
        Optional<String> result = crossRefClient.fetchDataForDoi(DoiString);
        assertThat(result.isEmpty(), is(true));
    }

    private CrossRefClient crossRefClientReceives404() {
        HttpResponseStatus404<String> errorResponse = new HttpResponseStatus404<String>(
            ERROR_MESSAGE);
        MockHttpClient<String> mockHttpClient = new MockHttpClient<>(errorResponse);
        CrossRefClient crossRefClient = new CrossRefClient(mockHttpClient);
        crossRefClient.setLogger(logger);
        return crossRefClient;
    }

    private CrossRefClient crossRefClientReceives500() {
        HttpResponseStatus500<String> errorResponse = new HttpResponseStatus500<String>(
            ERROR_MESSAGE);
        MockHttpClient<String> mockHttpClient = new MockHttpClient<>(errorResponse);
        CrossRefClient crossRefClient = new CrossRefClient(mockHttpClient);
        crossRefClient.setLogger(logger);
        return crossRefClient;
    }

    private void targetURlReturnsAValidUrlForDoiStrings(String doiPrefix)
        throws URISyntaxException {
        String doiURL = String.join("/", doiPrefix, DoiString);

        String expected = String.join("/", CrossRefClient.CROSSREF_LINK, WORKS, DoiString);

        String output = crossRefClient.createUrlToCrossRef(doiURL).toString();
        assertThat(output, is(equalTo(expected)));
    }

}
