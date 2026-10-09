package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.databind.ObjectMapper;

class StationPollingServiceTest {
    @Test
    void pollingPropertiesTest() {
        var PollingProperties = new StationPollingService.PollingProperties(20, 18000, 100);
        assertEquals(20, PollingProperties.batchSize());
        assertEquals(18000, PollingProperties.intervalMs());
        assertEquals(100, PollingProperties.maxRequestsPerMinute());

        assertThrows(
                IllegalArgumentException.class,
                () -> new StationPollingService.PollingProperties(0, 18000, 100));
        assertThrows(
                IllegalArgumentException.class,
                () -> new StationPollingService.PollingProperties(20, 0, 100));
        assertThrows(
                IllegalArgumentException.class,
                () -> new StationPollingService.PollingProperties(20, 18000, 50));
    }

    @Test
    void batchSizeTest() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new StationPollingService(
                                new StationRepository(
                                        new ObjectMapper(),
                                        new ClassPathResource("data/stations.json")),
                                null,
                                null,
                                new StationPollingService.PollingProperties(500, 60000, 1000)));
    }
}
