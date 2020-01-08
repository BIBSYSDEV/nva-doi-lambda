package no.unit.nva.doi;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class DataciteConnectionErrorTest {

    public static final String SOME_ERROR_MESSAGE = "Something";

    @Test
    public void exists() {
        Assertions.assertThrows(DataciteConnectionError.class, () -> {
            DataciteConnectionError error = new DataciteConnectionError(SOME_ERROR_MESSAGE);
            Assertions.assertEquals(SOME_ERROR_MESSAGE, error.getMessage());
            throw error;
        });
    }

}