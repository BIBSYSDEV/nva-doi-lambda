package no.unit.nva.doi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ExecutionException;

public class DataciteClient {

    private static final String dataciteBaseUrlString = "https://data.datacite.org";
    //private static final String CROSSREF_LINK = "http://api.crossref.org/works/";
    private final transient HttpClient httpClient;

    public DataciteClient() {
        this(HttpClient.newHttpClient());
    }

    public DataciteClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected URL createRequestUrl(String doiUrlString, DataciteContentType dataciteContentType)
        throws MalformedURLException {
        URL dataciteBaseUrl = new URL(dataciteBaseUrlString);
        URL doiUrl = new URL(doiUrlString);
        return new URL(dataciteBaseUrl,
                       String.join("",
                                   dataciteContentType.getContentType(),
                                   doiUrl.getPath()));
    }

    public String fetchMetadata(String doiUrlString, DataciteContentType dataciteContentType)
        throws IOException, InterruptedException, ExecutionException {
        return readStringFromUrl(createRequestUrl(doiUrlString, dataciteContentType));
    }

    protected String readStringFromUrl(URL url) throws ExecutionException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url.toString())).GET().build();
        String response = httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
                                    .thenApply(HttpResponse::body)
                                    .get();
        return response;
    }
}
