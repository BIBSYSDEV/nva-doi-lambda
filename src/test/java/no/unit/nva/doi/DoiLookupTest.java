package no.unit.nva.doi;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.MalformedURLException;
import org.junit.Test;

public class DoiLookupTest {

    public static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    @Test
    public void test() throws MalformedURLException {
        DoiLookup doiLookup = new DoiLookup();
        doiLookup.setDoi("https://doi.org/10.1109/5.771073");

        String json = gson.toJson(doiLookup);
        System.out.println(json);

        DoiLookup processedDoiLookup = gson.fromJson(json, DoiLookup.class);

        assertEquals(processedDoiLookup.getDoi(), doiLookup.getDoi());
    }
}
