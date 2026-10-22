package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.ingestion.trips.TripsClient;
import dev.leonardo.bvgtransit.ingestion.trips.TripsPollingService;
import dev.leonardo.bvgtransit.ingestion.trips.TripsStore;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class TripsPollingServiceTest {

    @Test
    void pollNextTrips() {
        FakeTripsClient tripsClient = new FakeTripsClient();
        FakeTripsStore tripsStore = new FakeTripsStore();

        ObjectMapper objectMapper = new ObjectMapper();
        var service = new TripsPollingService(tripsClient, tripsStore, objectMapper);
        service.pollNextTrips().collectList().block(Duration.ofSeconds(2));

        var requested = tripsClient.getRequested();
        assertFalse(requested.isEmpty());
        for (String tripId : requested.keySet()) {
            assertTrue(tripsStore.containsKey(tripId));
            assertEquals(
                    objectMapper.readTree(requested.get(tripId)),
                    objectMapper.readTree(tripsStore.get(tripId)));
        }
    }

    private static class FakeTripsClient implements TripsClient {

        private final Map<String, String> requested = new HashMap<>();

        @Override
        public @NonNull Mono<String> fetchTrips() {
            String trip1 =
                    "{ "
                            + "\"stopovers\": [ "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-1\" }}, "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-2\" }} "
                            + "], "
                            + "\"id\": \"aaa\" "
                            + "}";
            trip1 = trip1.strip();
            requested.put("aaa", trip1);

            String trip2 =
                    "{ "
                            + "\"stopovers\": [ "
                            + "{ \"stop\": { \"id\": \"stop-id-3\", \"name\": \"stop-name-3\" }}, "
                            + "{ \"stop\": { \"id\": \"stop-id-4\", \"name\": \"stop-name-3\" }} "
                            + "], "
                            + "\"id\": \"bbb\" "
                            + "}";
            trip2 = trip2.strip();
            requested.put("bbb", trip2);

            String responseBody = "{ \"trips\": [ " + trip1 + "," + trip2 + "] }";
            return Mono.just(responseBody);
        }

        @Override
        public @NonNull Mono<String> fetchTrip(String tripID) {
            String trip1 =
                    "{ "
                            + "\"stopovers\": [ "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-1\" }}, "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-2\" }} "
                            + "], "
                            + "\"id\": \"aaa\" "
                            + "}";
            trip1 = trip1.strip();
            return Mono.just(trip1);
        }

        public Map<String, String> getRequested() {
            return requested;
        }
    }

    private static class FakeTripsStore implements TripsStore {
        final Map<String, String> saved = new HashMap<>();

        private String keyForTrip(String tripId) {
            return "trip:".concat(tripId);
        }

        @Override
        public @NonNull Mono<Boolean> storeTrip(String tripID, String json) {
            saved.put(keyForTrip(tripID), json);
            return Mono.just(true);
        }

        public boolean containsKey(String tripID) {
            return saved.containsKey(keyForTrip(tripID));
        }

        public String get(String tripId) {
            return saved.get(keyForTrip(tripId));
        }
    }
}
