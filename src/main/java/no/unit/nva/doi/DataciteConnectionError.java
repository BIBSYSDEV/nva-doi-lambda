package no.unit.nva.doi;

import java.io.IOException;

public class DataciteConnectionError extends IOException {
    public DataciteConnectionError(String message) {
        super(message);
    }
}
