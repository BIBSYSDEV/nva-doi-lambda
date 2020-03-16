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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

public class CrossRefClient {

    public static final String CROSSREF_LINK = "https://api.crossref.org";
    public static final String WORKS = "works";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    public static final int TIMEOUT_DURATION = 30;
    public static final String COULD_NOT_FIND_ENTRY_WITH_DOI = "Could not find entry with DOI:";
    private final transient HttpClient httpClient;
    private final transient LambdaLogger logger;

    public CrossRefClient(LambdaLogger logger) {
        this(HttpClient.newHttpClient(), logger);
    }

    public CrossRefClient(HttpClient httpClient, LambdaLogger logger) {
        this.httpClient = httpClient;
        this.logger = logger;
    }

    public Optional<String> fetchDataForDoi(String doiIdentifier) throws URISyntaxException {
        URI targetUri = createTargetUrl(doiIdentifier);
        return fetchJson(targetUri);
    }

    private Optional<String> fetchJson(URI doiUri) {
        HttpRequest request = createRequest(doiUri);
        try {
            return Optional.ofNullable(getFromWeb(request));
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
                          .header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
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
        throw new BadRequestException("Something went wrong");
    }

    private boolean responseIsSuccessful(HttpResponse<String> response) {
        return response.statusCode() == HttpStatus.SC_OK;
    }

    protected URI createTargetUrl(String doiIdentifier)
        throws URISyntaxException {

        List<String> doiPathSegments = stripHttpPartFromDoi(doiIdentifier);
        List<String> pathSegments = composeAllPathSegments(doiPathSegments);
        return completeUrlToCrossRef(pathSegments);

    }

    private URI completeUrlToCrossRef(List<String> pathSegments) throws URISyntaxException {
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
        return URLEncodedUtils.parsePathSegments(path);
    }

}
