package no.unit.nva.doi;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum DataciteContentType {

    @SerializedName("application/vnd.citationstyles.csl+json")
    CITEPROC_JSON("application/vnd.citationstyles.csl+json"),
    @SerializedName("application/vnd.datacite.datacite+json")
    DATACITE_JSON("application/vnd.datacite.datacite+json"),
    @SerializedName("application/vnd.datacite.datacite+xml")
    DATACITE_XML("application/vnd.datacite.datacite+xml");

    private final String contentType;

    DataciteContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Look up enum for Datacite Content Type.
     *
     * @param contentType contentType
     * @return DataciteContentType
     */
    public static DataciteContentType lookup(String contentType) {
        return Arrays.stream(DataciteContentType.values())
                     .filter(dataciteContentType -> dataciteContentType.getContentType()
                                                                       .equals(contentType))
                     .findAny()
                     .orElseThrow(() ->
                                      new IllegalArgumentException(
                                          String.format(
                                              "Datacite Content Type not found for '%s', expected one of '%s'.",
                                              contentType,
                                              String.join(",", Arrays
                                                  .stream(DataciteContentType.values())
                                                  .map(DataciteContentType::getContentType)
                                                  .collect(Collectors.joining(",")))))
                     );
    }

    public String getContentType() {
        return contentType;
    }
}
