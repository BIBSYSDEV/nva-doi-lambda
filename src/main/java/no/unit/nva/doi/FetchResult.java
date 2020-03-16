package no.unit.nva.doi;

public class FetchResult {

    private final String contentHeader;
    private final String json;

    public FetchResult(String contentHeader, String json) {
        this.contentHeader = contentHeader;
        this.json = json;
    }

    public String getContentHeader() {
        return contentHeader;
    }

    public String getJson() {
        return json;
    }
}
