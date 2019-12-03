package no.unit.nva.doi;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

class MetadataMap {

    Map<String, List<String>> map;

    public MetadataMap() {
        this.map = new TreeMap<>();
    }

    public MetadataMap(Document doc) {
        this.map = new TreeMap<>();
        this.meta(doc);
    }

    void meta(Document document) {
        final Element head = document.head();
        final Elements elements = head != null ? head.getElementsByTag("meta") : null;
        this.attributeContents(elements);
    }

    private void attributeContents(Elements elements) {
        if (elements != null) {
            for (Element element : elements) {
                final Attributes attributes = element.attributes();
                String key = attributes.get("name");
                if (key == null || key.isEmpty()) {
                    key = attributes.get("property");
                }
                String value = attributes.get("content");
                if (key != null && !key.isEmpty()) {
                    this.put(key, value);
                }
            }
        }
    }

    String one(String key) {
        return this.all(key).stream().findFirst().orElse(null);
    }

    List<String> all(String key) {
        final List<String> all = new ArrayList<>();
        final List<String> values = this.map.get(key);
        if (values != null) {
            all.addAll(values);
        }
        return all;
    }

    void put(String key, List<String> valueList) {
        List<String> values = this.map.get(key);
        if (values == null) {
            values = new ArrayList<>();
        }
        if (valueList != null && !valueList.isEmpty()) {
            values.addAll(valueList);
            this.map.put(key, values.stream().sorted().distinct().collect(Collectors.toList()));
        }
    }

    void put(String key, String value) {
        List<String> values = this.map.get(key);
        if ((values == null)) {
            values = new ArrayList<>();
        }
        if ((value != null) && !value.isEmpty()) {
            values.add(value);
            this.map.put(key, values.stream().sorted().distinct().collect(Collectors.toList()));
        }
    }

    public Map<String, List<String>> getMap() {
        return map;
    }

    public void putAll(MetadataMap extract_metadata_dc_doi) {
        this.map.putAll(extract_metadata_dc_doi.getMap());
    }

    public boolean containsKey(String s) {
        return this.map.containsKey(s);
    }

    public String getFirst(String s) {
        return this.map.get(s).get(0);
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
