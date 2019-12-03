package no.unit.nva.doi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

class DoiMetadata {
    String type;
    String id;

    final List<String> categories = new ArrayList<>();

    @SerializedName(value = "abstract")
    String description_abstract;

    String publisher;

    @SerializedName(value = "author")
    final
    List<Author> authors = new ArrayList<>();

    @SerializedName(value = "container-title")
    String title_container;

    String title;
    String page;
    String volume;

    Issued issued;
}
