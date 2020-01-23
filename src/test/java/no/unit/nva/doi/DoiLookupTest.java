package no.unit.nva.doi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class DoiLookupTest {

    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Test
    public void test() throws MalformedURLException {
        DoiLookup doiLookup = new DoiLookup();
        doiLookup.setDoiUrl(new URL("https://doi.org/10.1109/5.771073"));
        doiLookup.setDataciteContentType(DataciteContentType.CITEPROC_JSON);

        String json = gson.toJson(doiLookup);
        System.out.println(json);

        DoiLookup processedDoiLookup = gson.fromJson(json, DoiLookup.class);

        assertEquals(processedDoiLookup.getDoiUrl(), doiLookup.getDoiUrl());
        assertEquals(processedDoiLookup.getDataciteContentType(), doiLookup.getDataciteContentType());

    }


}
