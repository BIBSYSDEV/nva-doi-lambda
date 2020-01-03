package no.unit.nva.doi;

public class SimpleResponse {

    private String body;


    public SimpleResponse(String payload) {
        this.body = payload;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
