package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.ingestion.departures.DeparturesClient;
import dev.leonardo.bvgtransit.ingestion.departures.DeparturesPollingService;
import dev.leonardo.bvgtransit.ingestion.departures.DeparturesStore;
import dev.leonardo.bvgtransit.ingestion.station.StationCatalog;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import reactor.core.publisher.Mono;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DeparturesPollingServiceTest {
    @Test
    void pollingPropertiesTest() {
        var PollingProperties = new DeparturesPollingService.PollingProperties(20, 18000, 100);
        assertEquals(20, PollingProperties.batchSize());
        assertEquals(18000, PollingProperties.intervalMs());
        assertEquals(100, PollingProperties.maxRequestsPerMinute());

        assertThrows(
                IllegalArgumentException.class,
                () -> new DeparturesPollingService.PollingProperties(0, 18000, 100));
        assertThrows(
                IllegalArgumentException.class,
                () -> new DeparturesPollingService.PollingProperties(20, 0, 100));
        assertThrows(
                IllegalArgumentException.class,
                () -> new DeparturesPollingService.PollingProperties(20, 18000, 50));
    }

    @Test
    void batchSizeTest() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new DeparturesPollingService(
                                new StationCatalog(
                                        new ObjectMapper(),
                                        new ClassPathResource("data/stations.json")),
                                null,
                                null,
                                new DeparturesPollingService.PollingProperties(500, 60000, 1000)));
    }

    @Test
    void pollNextBatchTest() throws IOException {
        int batchSize = 10;
        FakeDeparturesClient departuresClient = new FakeDeparturesClient();
        FakeDeparturesStore stationDepartureStore = new FakeDeparturesStore();

        var service =
                new DeparturesPollingService(
                        new StationCatalog(
                                new ObjectMapper(), new ClassPathResource("data/stations.json")),
                        departuresClient,
                        stationDepartureStore,
                        new DeparturesPollingService.PollingProperties(batchSize, 15000, 100));

        service.pollNextBatch().block(Duration.ofSeconds(2));
        List<String> requested = departuresClient.getRequested();
        assertEquals(batchSize, requested.size());

        for (String stationID : requested) {
            assertEquals(true, stationDepartureStore.containsKey(stationID));
        }
    }

    private static class FakeDeparturesClient implements DeparturesClient {
        private final List<String> requested = new ArrayList<>();

        @Override
        public @NonNull Mono<String> fetchDepartures(String stationID) {
            requested.add(stationID);
            return Mono.just("{\"station\": stationID, \"departures:\" [] }");
        }

        public List<String> getRequested() {
            return requested;
        }
    }

    private static class FakeDeparturesStore implements DeparturesStore {
        final Map<String, String> saved = new HashMap<>();

        private String keyForStation(String stationID) {
            return stationID;
        }

        @Override
        public @NonNull Mono<Boolean> storeDepartures(String stationID, String json) {
            saved.put(keyForStation(stationID), json);
            return Mono.just(true);
        }

        public boolean containsKey(String stationID) {
            return saved.containsKey(keyForStation(stationID));
        }
    }
}
