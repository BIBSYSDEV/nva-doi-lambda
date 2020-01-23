package no.unit.nva.doi;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum DataciteContentType {

    @SerializedName("citeproc_json")
    CITEPROC_JSON("application/vnd.citationstyles.csl+json"),
    @SerializedName("datacite_json")
    DATACITE_JSON("application/vnd.datacite.datacite+json"),
    @SerializedName("datacite_xml")
    DATACITE_XML("application/vnd.datacite.datacite+xml");

    private final String contentType;

    DataciteContentType(String contentType) {
        this.contentType = contentType;
    }

    public static DataciteContentType lookup(String name) {
        try {
            return DataciteContentType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Datacite Content Type not found for '%s', expected one of '%s'.", name, String.join(",", Arrays.stream(DataciteContentType.values()).map(DataciteContentType::name).collect(Collectors.joining(",")))));
        }
    }

    public String getContentType() {
        return contentType;
    }
}
