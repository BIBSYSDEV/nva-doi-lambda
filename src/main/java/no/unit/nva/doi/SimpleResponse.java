package no.unit.nva.doi;

public class SimpleResponse {

    String body;
    String statusCode;

    public SimpleResponse(String payload, String statusCode) {
        this.body = payload;
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
