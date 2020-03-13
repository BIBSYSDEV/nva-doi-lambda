package no.unit.nva.utils;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

public class MockHttpClient<R> extends HttpClient {

    private final R response;

    public MockHttpClient(R response) {
        this.response = response;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return null;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            BodyHandler<T> responseBodyHandler) {

        CompletableFuture<HttpResponse<T>> result = CompletableFuture
            .completedFuture(new HttpResponseImpl<T>(request, (T) response));
        return result;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            BodyHandler<T> responseBodyHandler,
                                                            PushPromiseHandler<T> pushPromiseHandler) {
        return null;
    }

    public static class HttpResponseImpl<S> implements HttpResponse<S> {

        private final HttpRequest request;
        private final S body;

        public HttpResponseImpl(HttpRequest request,
                                S body) {
            this.request = request;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return 0;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<S>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public S body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public Version version() {
            return null;
        }
    }
}
