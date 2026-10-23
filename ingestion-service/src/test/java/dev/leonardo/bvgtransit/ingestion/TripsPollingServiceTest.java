package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.ingestion.config.PollingProperties;
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

    private String keyForTrip(String tripID) throws IllegalArgumentException {
        String[] parts = tripID.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid trip ID: " + tripID + "; Expected 5 numbers got " + parts.length);
        }
        return String.join("|", parts[0], parts[1], parts[3]);
    }

    @Test
    void pollNextTrips() {
        FakeTripsClient tripsClient = new FakeTripsClient();
        FakeTripsStore tripsStore = new FakeTripsStore();

        ObjectMapper objectMapper = new ObjectMapper();
        var service =
                new TripsPollingService(
                        tripsClient,
                        tripsStore,
                        objectMapper,
                        new PollingProperties(20, 15000, 100, 15));
        service.pollNextTrips().collectList().block(Duration.ofSeconds(2));

        var requested = tripsClient.getRequested();
        assertFalse(requested.isEmpty());
        for (String tripId : requested.keySet()) {
            var storeKey = keyForTrip(tripId);
            assertTrue(tripsStore.containsKey(storeKey));
            assertEquals(
                    objectMapper.readTree(requested.get(tripId)),
                    objectMapper.readTree(tripsStore.get(storeKey)));
        }
    }

    private static class FakeTripsClient implements TripsClient {

        private final Map<String, String> requested = new HashMap<>();

        @Override
        public @NonNull Mono<String> fetchTrips() {

            String tripId1 = "1|68265|0|86|21102026";
            String trip1 =
                    "{ "
                            + "\"stopovers\": [ "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-1\" }}, "
                            + "{ \"stop\": { \"id\": \"stop-id-1\", \"name\": \"stop-name-2\" }} "
                            + "], "
                            + "\"id\": \""
                            + tripId1
                            + "\" "
                            + "}";
            trip1 = trip1.strip();
            requested.put(tripId1, trip1);

            String tripId2 = "2|99265|0|86|21102026";
            String trip2 =
                    "{ "
                            + "\"stopovers\": [ "
                            + "{ \"stop\": { \"id\": \"stop-id-3\", \"name\": \"stop-name-3\" }}, "
                            + "{ \"stop\": { \"id\": \"stop-id-4\", \"name\": \"stop-name-3\" }} "
                            + "], "
                            + "\"id\": \""
                            + tripId2
                            + "\" "
                            + "}";
            trip2 = trip2.strip();
            requested.put(tripId2, trip2);

            String responseBody = "{ \"trips\": [ " + trip1 + "," + trip2 + "] }";
            return Mono.just(responseBody);
        }

        @Override
        public @NonNull Mono<String> fetchTrip(String tripID) {
            String trip = "{ \"trip\":" + requested.get(tripID) + " }";
            trip = trip.strip();
            return Mono.just(trip);
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
        public @NonNull Mono<Boolean> containsTrip(String tripID) {
            return Mono.just(saved.containsKey(keyForTrip(tripID)));
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
