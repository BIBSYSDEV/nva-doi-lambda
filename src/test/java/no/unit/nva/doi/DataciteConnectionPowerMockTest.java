package no.unit.nva.doi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, DataciteConnection.class })
public class DataciteConnectionPowerMockTest {

    public static final String MOCK_URL = "http://example.org/123";
    public static final String MOCK_BODY = "Svenn";

    @Test
    public void test_successful_connect() throws Exception {
        URLConnection conn = PowerMockito.mock(URLConnection.class);
        URL mockUrl = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(mockUrl);
        DataciteConnection dataciteConnection = new DataciteConnection(mockUrl);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(MOCK_BODY.getBytes());
        PowerMockito.when(mockUrl.openConnection()).thenReturn(conn);
        PowerMockito.when(conn.getInputStream()).thenReturn(inputStream);
        String content = dataciteConnection.connect(MOCK_URL);
        assertEquals(MOCK_BODY, content);
    }

}