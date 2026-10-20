package dev.leonardo.bvgtransit.ingestion;

import dev.leonardo.bvgtransit.ingestion.station.StationCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StationCatalogTest {

    @Test
    void loadStationsFromClassPath() throws IOException {
        var repository = new StationCatalog(new ObjectMapper(), new ClassPathResource("data/stations.json"));
        assertFalse(repository.getStations().isEmpty());
        assertNotNull(repository.getStations().getFirst().id());
    }

}
