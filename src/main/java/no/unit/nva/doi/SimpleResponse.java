package no.unit.nva.doi;

public class SimpleResponse {

    private String body;
    private String statusCode;


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
}
