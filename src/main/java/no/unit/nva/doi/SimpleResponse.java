package no.unit.nva.doi;

import javax.ws.rs.core.Response;

public class SimpleResponse {

    private String body;
    private int statusCode;


    public SimpleResponse(String payload, int statusCode) {
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
