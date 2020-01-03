package no.unit.nva.doi;

public class SimpleResponse {

    private String body;
    private int statusCode;


    public SimpleResponse(String body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public SimpleResponse(String payload) {
        this.body = payload;
        this.statusCode = 200;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
