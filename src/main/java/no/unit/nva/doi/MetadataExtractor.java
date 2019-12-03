package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataExtractor {

    private final Logger logger = LoggerFactory.getLogger(MetadataExtractor.class);

    public static final String _HTTP_ = "http";
    public static final String HTTPS = "https://";
    public static final String HTTP = "http://";
    public static final String DOI_ORG = "doi.org";
    public static final String DOI = "doi:";
    private final String DATACITE_URL = "https://data.datacite.org/application/vnd.citationstyles.csl+json";

    private int external_service_timeout;

    public MetadataExtractor() {
        external_service_timeout = 15000;
    }

    public MetadataExtractor(int externalServiceTimeout) {
        external_service_timeout = externalServiceTimeout;
    }

    public int getExternal_service_timeout() {
        return external_service_timeout;
    }

    MetadataMap extract_metadata_from_resource_content(String content) throws IOException {
        MetadataMap dc_metadata_extracted = new MetadataMap();
        if (content.startsWith(HTTPS) || content.startsWith(HTTP)) {
            try {
                dc_metadata_extracted = extract_metadata_from_url(content);
            } catch (UnsupportedMimeTypeException e) {
                logger.info(e.getMimeType() + " " + e.getMessage() + " " + e.getUrl());
            }
        }
        return dc_metadata_extracted;
    }

    MetadataMap extract_metadata_from_url(String url) throws IOException {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost();
            final MetadataMap metadataMap;
            if (host.contains(DOI_ORG)) {
                metadataMap = extract_metadata_dc_doi(url);
                final Document doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(external_service_timeout).get();
                metadataMap.putAll(extract_metadata_dc_and_og_default(doc));
            } else {
                throw new IllegalArgumentException("Given URL is not a DOI-url: " + url);
            }
            followForwardingDoiIdentifier(url, metadataMap);
            return metadataMap;
        } catch (URISyntaxException e) {
            final String s = e.getMessage() + " " + url;
            logger.error(s, e);
            throw new IOException(s, e);
        }
    }

    void followForwardingDoiIdentifier(String url, MetadataMap metadataMap) {
        if (metadataMap.containsKey("dc.identifier.doi")) {
            final String doi = metadataMap.getFirst("dc.identifier.doi");
            if (!url.equals(doi)) {
                final MetadataMap dc_metadata_doi = extract_metadata_dc_doi(doi);
                for (String key : dc_metadata_doi.keySet()) {
                    metadataMap.put(key, dc_metadata_doi.getFirst(key));
                }
            }
        }
    }

    MetadataMap extract_metadata_dc_doi(String url) {
        MetadataMap map = new MetadataMap();
        try {
            final DoiMetadata doiMetadata = getDoiMetadata(url);
            map.put("dc.publisher", doiMetadata.publisher);
            map.put("dc.description.abstract", doiMetadata.description_abstract);
            map.put("dc.identifier.doi", url);
            map.put("dc.source.pagenumber", doiMetadata.page);
            map.put("dc.source.volume", doiMetadata.volume);
            if (doiMetadata.type != null && doiMetadata.type.contains("journal") && doiMetadata.title_container != null) {
                map.put("dc.source.journal", doiMetadata.title_container);
            }

            map.put("dc.title", doiMetadata.title);
            for (Author author : doiMetadata.authors) {
                if (author.family != null && author.given != null) {
                    map.put("dc.contributor.author", author.family + ", " + author.given);
                }
                if (author.literal != null) {
                    map.put("dc.contributor.author", author.literal);
                }
            }

            for (String category : doiMetadata.categories) {
                map.put("dc.subject", category);
            }
            map.put("dc.type", doiMetadata.type);

            map.put("dc.date.issued", issued(doiMetadata.issued));
        } catch (IOException e) {
            logger.error(e.getMessage() + " " + url, e);
        }
        return map;
    }

    private String issued(Issued issued) {
        StringBuilder r = new StringBuilder();
        if (issued != null) {
            final Optional<List<Integer>> optional = issued.date_parts.stream().findFirst();
            if (optional.isPresent()) {
                final List<Integer> list = optional.get();
                for (Integer integer : list) {
                    final String s = integer.toString();
                    r.append(s.length() == 1 ? "0" + s : s);
                }
            }
        }
        return r.toString();
    }

    private DoiMetadata getDoiMetadata(String doi) throws IOException {
        final String json = getDoiMetadata_json(doi);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, DoiMetadata.class);
    }

    String getDoiMetadata_json(String doi) throws IOException {
        final String doiPath;
        try {
            doiPath = new URI(doi).getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage() + " " + doi, e);
        }
        final String dataCite = DATACITE_URL + doiPath;
        final Connection connection = Jsoup.connect(dataCite).timeout(external_service_timeout).ignoreContentType(true);
        return connection.get().wholeText();
    }

    MetadataMap meta(String content) throws IOException {
        final Document doc = Jsoup.connect(content).timeout(external_service_timeout).get();
        return new MetadataMap(doc);
    }

    MetadataMap extract_metadata_dc_and_og_default(Document doc) throws IOException {
        final MetadataMap map = new MetadataMap();
        try {
            final MetadataMap meta = new MetadataMap(doc);

            final String title = doc.title();
            final String description = meta.one("description") != null ? meta.one("description") : meta.one("Description");
            final String og_title = meta.one("og:title");
            final String og_description = meta.one("og:description");

            meta.all("citation_author").forEach(s -> map.put("dc.contributor.author", s));
            meta.all("keywords").stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim).collect(Collectors.toList())
                    .forEach(t -> map.put("dc.subject", t));

            meta.all("Keywords").stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim).collect(Collectors.toList())
                    .forEach(t -> map.put("dc.subject", t));
            meta.all("article:tag").stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim).collect(Collectors.toList())
                    .forEach(t -> map.put("dc.subject", t));

            meta.all("WT.z_subject_term").stream()
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .map(String::trim).collect(Collectors.toList())
                    .forEach(t -> map.put("dc.subject", t));

            final String dc_identifier = meta.one("dc.identifier") != null ? meta.one("dc.identifier") : meta.one("DC.identifier");

            String doi = dc_identifier != null && dc_identifier.startsWith(DOI) ? dc_identifier.replace(DOI, "") : meta.one("citation_doi");
            if (doi != null && !doi.isEmpty()) {
                doi = doi.startsWith(_HTTP_) ? doi : HTTPS + DOI_ORG + "/" + doi;
                map.putAll(extract_metadata_dc_doi(doi)); //vurder å kunne velge om vi skal følge doi lenke
            }

            map.put("dc.publisher", meta.one("DC.publisher"));
            map.put("dc.publisher", meta.one("Publisher"));
            map.put("dc.publisher", meta.one("publisher"));
            map.put("dc.publisher", meta.one("og:site_name"));
            map.put("dc.rights", meta.one("dc.rights"));
            map.put("dc.creator", meta.one("dc.creator"));
            map.put("dc.creator", meta.one("DC.creator"));
            map.put("dc.rightsAgent", meta.one("dc.rightsAgent"));
            map.put("dc.language", meta.one("dc.language"));
            map.put("dc.source", meta.one("dc.source"));
            map.put("dc.format", meta.one("dc.format"));
            map.put("dc.type", meta.one("dc.type"));
            map.put("dc.type", meta.one("DC.type"));
            map.put("dc.identifier.doi", doi);
            map.put("dc.identifier.issn", meta.one("citation_issn"));
            map.put("dc.title", (og_title != null && !og_title.isEmpty() ? og_title : title));
            map.put("dc.description.abstract", (og_description != null && !og_description.isEmpty()) ? og_description : description);

            map.put("og:title", og_title);
            map.put("og:description", og_description);
            map.put("og:type", meta.one("og:type"));
            map.put("og:url", meta.one("og:url"));
            map.put("og:updated_time", meta.one("og:updated_time"));
            map.put("og:site_name", meta.one("og:site_name"));
            map.put("og:image", meta.all("og:image"));
            map.put("og:image:alt", meta.one("og:image:alt"));
            map.put("og:image:type", meta.one("og:image:type"));
            map.put("og:image:height", meta.one("og:image:height"));
            map.put("og:image:width", meta.one("og:image:width"));
            map.put("og:image:secure_url", meta.one("og:image:secure_url"));
            map.put("og:video", meta.one("og:video"));
            map.put("og:video:secure_url", meta.one("og:video:secure_url"));
            map.put("og:video:type", meta.one("og:video:type"));
            map.put("og:video:width", meta.one("og:video:width"));
            map.put("og:video:height", meta.one("og:video:height"));
            map.put("og:locale", meta.one("og:locale"));
            map.put("og:locale:alternate", meta.all("og:locale:alternate"));

            map.put("og:audio", meta.one("og:audio"));
            map.put("og:audio:secure_url", meta.one("og:audio:secure_url"));
            map.put("og:audio:type", meta.one("og:audio:type"));

            map.put("citation_abstract_html_url", meta.one("citation_abstract_html_url"));
            map.put("citation_abstract", meta.all("citation_abstract"));
            map.put("citation_article_type", meta.one("citation_article_type"));
            map.put("citation_author", meta.all("citation_author"));
            map.put("citation_author_email", meta.all("citation_author_email"));
            map.put("citation_author_institution", meta.all("citation_author_institution")); //todo: fiks parsing av institution pr author (må ta hensyn til rekkefølge på taggene)
            map.put("citation_cover_date", meta.one("citation_cover_date"));
            map.put("citation_doi", meta.one("citation_doi"));
            map.put("citation_firstpage", meta.one("citation_firstpage"));
            map.put("citation_fulltext_html_url", meta.one("citation_fulltext_html_url"));
            map.put("citation_issn", meta.one("citation_issn"));
            map.put("citation_issue", meta.one("citation_issue"));
            map.put("citation_journal_abbrev", meta.one("citation_journal_abbrev"));
            map.put("citation_journal_title", meta.one("citation_journal_title"));
            map.put("citation_language", meta.one("citation_language"));
            map.put("citation_lastpage", meta.one("citation_lastpage"));
            map.put("citation_online_date", meta.one("citation_online_date"));
            map.put("citation_pdf_url", meta.one("citation_pdf_url"));
            map.put("citation_xml_url", meta.one("citation_xml_url"));
            map.put("citation_publisher", meta.one("citation_publisher"));
            map.put("citation_publication_date", meta.one("citation_publication_date"));
            map.put("citation_bibcode", meta.one("citation_bibcode"));
            map.put("citation_title", meta.one("citation_title"));
            map.put("citation_volume", meta.one("citation_volume"));
            map.put("citation_reference", meta.all("citation_reference"));

            map.put("prism.copyright", meta.one("prism.copyright"));
            map.put("prism.publicationName", meta.one("prism.publicationName"));
            map.put("prism.coverDisplayDate", meta.one("prism.coverDisplayDate"));
            map.put("prism.volume", meta.one("prism.volume"));
            map.put("prism.issueName", meta.one("prism.issueName"));
            map.put("prism.section", meta.one("prism.section"));
            map.put("prism.elssn", meta.one("prism.elssn"));
            map.put("prism.doi", meta.one("prism.doi"));
            map.put("prism.url", meta.one("prism.url"));

            map.put("WT.page_categorisation", meta.one("WT.z_primary_atype"));
            map.put("WT.z_primary_atype", meta.one("WT.z_primary_atype"));
            map.put("WT.z_subject_term", meta.one("WT.z_subject_term"));
            map.put("WT.z_subject_term_id", meta.one("WT.z_subject_term"));
        } catch (Exception e) {
            final String s = e.getMessage();
            logger.error(s, e);
            throw new IOException(s, e);
        }
        return map;
    }

}
