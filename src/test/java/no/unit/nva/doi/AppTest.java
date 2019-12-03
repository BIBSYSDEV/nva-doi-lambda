package no.unit.nva.doi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AppTest {

  private App app;

  @Before
  public void setUp()  {
    app = new App();
  }

  @Test
  public void successfulResponse() {
    String url = "https://doi.org/10.1093/afraf/ady029";
    GatewayResponse result = (GatewayResponse) app.handleRequest(url, null);
    assertEquals(200, result.getStatusCode());
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains(url));
    assertTrue(content.contains("The political economy of banking in Angola"));
  }

  @Test
  public void testBadRequestResponse() {
    String url = "htps://doi.org/10.1093/afraf/ady029";
    GatewayResponse result = (GatewayResponse) app.handleRequest(url, null);
    assertEquals(400, result.getStatusCode());
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains("{\"error\": \"unknown protocol: htps\"}"));
  }

  @Test
  public void test_getValidURI_valid() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
    String url = "https://doi.org/10.1093/afraf/ady029";
    assertEquals(url, app.getValidURI(url, 1024));
  }

  @Test(expected = URISyntaxException.class)
  public void test_getValidURI_invalid() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
    String url = "https:// doi.org/";
    app.getValidURI(url, 1024);
  }

  @Test(expected = MalformedParametersException.class)
  public void test_getValidURI_invalidLength() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
    String url = "https://doi.org/10.1093/afraf/ady029";
    app.getValidURI(url, 10);
  }

  @Test(expected = MalformedParametersException.class)
  public void test_getValidURI_ullURL() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
    String url = "https://doi.org/10.1093/afraf/ady029";
    app.getValidURI(null, 1024);
  }


}
