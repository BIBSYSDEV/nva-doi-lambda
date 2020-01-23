package no.unit.nva.doi;

import java.net.URL;

public class DoiLookup {

    private URL doiUrl;
    private DataciteContentType dataciteContentType;


    public DoiLookup() {
    }

    public URL getDoiUrl() {
        return doiUrl;
    }

    public void setDoiUrl(URL doiUrl) {
        this.doiUrl = doiUrl;
    }

    public DataciteContentType getDataciteContentType() {
        return dataciteContentType;
    }

    public void setDataciteContentType(DataciteContentType dataciteContentType) {
        this.dataciteContentType = dataciteContentType != null ? dataciteContentType : DataciteContentType.CITEPROC_JSON;
    }
}
