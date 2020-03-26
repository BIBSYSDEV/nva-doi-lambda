package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

public class CrossRefClient {

    public static final String CROSSREF_LINK = "https://api.crossref.org";
    public static final String WORKS = "works";

    public static final int TIMEOUT_DURATION = 30;
    public static final String COULD_NOT_FIND_ENTRY_WITH_DOI = "Could not find entry with DOI:";
    public static final String UNKNOWN_ERROR_MESSAGE = "Something went wrong. StatusCode:";

    private static final String DOI_EXAMPLES = "10.1000/182, https://doi.org/10.1000/182";
    public static final String ILLEGAL_DOI_MESSAGE = "Illegal DOI:%s. Valid examples:" + DOI_EXAMPLES;

    private final transient HttpClient httpClient;
    private transient LambdaLogger logger;

    public CrossRefClient() {
        this(HttpClient.newHttpClient());
    }

    public CrossRefClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * The method returns the object containing the metadata (title, author, etc.) of the publication with the specific
     * DOI, and the source where the metadata were acquired.
     *
     * @param doiIdentifier a doi identifier or URL.
     * @return FetchResult contains the json object and the location from where it was fetched (Datacite, Crossref).
     * @throws URISyntaxException when the input cannot be transformed to a valid URI.
     */
    public Optional<MetadataAndContentLocation> fetchDataForDoi(String doiIdentifier) throws URISyntaxException {
        URI targetUri = createUrlToCrossRef(doiIdentifier);
        return fetchJson(targetUri);
    }

    private Optional<MetadataAndContentLocation> fetchJson(URI doiUri) {
        HttpRequest request = createRequest(doiUri);
        try {
            return Optional.ofNullable(getFromWeb(request))
                           .map(json -> new MetadataAndContentLocation(CROSSREF_LINK, json));
        } catch (InterruptedException |
            ExecutionException |
            NotFoundException |
            BadRequestException e) {
            System.out.print(e.getMessage());
            logger.log(e.getMessage());
            return Optional.empty();
        }
    }

    private HttpRequest createRequest(URI doiUri) {
        return HttpRequest.newBuilder(doiUri)
                          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                          .timeout(Duration.ofSeconds(TIMEOUT_DURATION))
                          .GET()
                          .build();
    }

    private String getFromWeb(HttpRequest request)
        throws InterruptedException, ExecutionException {
        HttpResponse<String> response = httpClient.sendAsync(request, BodyHandlers.ofString())
                                                  .get();
        if (responseIsSuccessful(response)) {
            return response.body();
        } else {
            return handleError(request, response);
        }
    }

    private String handleError(HttpRequest request, HttpResponse<String> response) {
        if (response.statusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(COULD_NOT_FIND_ENTRY_WITH_DOI + request.uri().toString());
        }
        throw new BadRequestException(UNKNOWN_ERROR_MESSAGE + response.statusCode());
    }

    private boolean responseIsSuccessful(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    protected URI createUrlToCrossRef(String doiIdentifier)
        throws URISyntaxException {
        List<String> doiPathSegments = stripHttpPartFromDoi(doiIdentifier);
        List<String> pathSegments = composeAllPathSegments(doiPathSegments);
        return addPathSegments(pathSegments);
    }

    private URI addPathSegments(List<String> pathSegments) throws URISyntaxException {
        return new URIBuilder(CROSSREF_LINK)
            .setPathSegments(pathSegments)
            .build();
    }

    private List<String> composeAllPathSegments(List<String> doiPathSegments) {
        List<String> pathSegments = new ArrayList<>();
        pathSegments.add(WORKS);
        pathSegments.addAll(doiPathSegments);
        return pathSegments;
    }

    private List<String> stripHttpPartFromDoi(String doiIdentifier) {
        String path = URI.create(doiIdentifier).getPath();
        if (Objects.isNull(path) || path.isBlank()) {
            throw new IllegalArgumentException(ILLEGAL_DOI_MESSAGE + doiIdentifier);
        }
        return URLEncodedUtils.parsePathSegments(path);
    }

    public void setLogger(LambdaLogger logger) {
        this.logger = logger;
    }
}
