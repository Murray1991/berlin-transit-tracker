package dev.leonardo.bvgtransit.ingestion.trips;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dev.leonardo.bvgtransit.ingestion.config.PollingProperties;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

@Component
public class TripsPollingService {

    private static final Logger log = LoggerFactory.getLogger(TripsPollingService.class);
    private final TripsClient tripsClient;
    private final TripsStore tripsStore;
    private final ObjectMapper objectMapper;
    private final PollingProperties pollingProperties;

    private final Map<String, TripDto> pendingTrips = new LinkedHashMap<>();

    public TripsPollingService(
            TripsClient tripsClient_,
            TripsStore tripsStore_,
            ObjectMapper objectMapper_,
            PollingProperties pollingProperties_) {
        tripsClient = tripsClient_;
        tripsStore = tripsStore_;
        objectMapper = objectMapper_;
        pollingProperties = pollingProperties_;
    }

    @Scheduled(fixedRate = 60000)
    public void pollTrips() {
        log.debug("Polling trips");
        pollNextTrips()
                .then()
                .subscribe(_ -> {}, error -> log.error("Storing trips failed", error));
    }

    /**
     * Poll next trips. Each polled trip is stored in a pending trips map. Then, we fetch the
     * detailed trips from the pending list (with complete list of stops) and cache them.
     */
    public @NonNull Flux<Boolean> pollNextTrips() {
        return tripsClient
                .fetchTrips()
                .flatMap(json -> Mono.just(parseTrips(json)))
                .flatMapMany(trips -> Flux.fromIterable(getPending(trips)))
                .filterWhen(this::isPending)
                .take(pollingProperties.maxTripDetailsPerPoll())
                .flatMap(e -> fetchAndStore(e.getKey(), e.getValue()));
    }

    private @NonNull Mono<Boolean> isPending(Map.Entry<String, TripDto> trip) {
        return tripsStore
                .containsTrip(trip.getKey())
                .doOnNext(
                        cached -> {
                            if (cached) {
                                synchronized (this) {
                                    pendingTrips.remove(trip.getKey());
                                }
                            }
                        })
                .map(cached -> !cached);
    }

    private synchronized List<Map.Entry<String, TripDto>> getPending(List<TripDto> trips) {
        trips.forEach(trip -> pendingTrips.putIfAbsent(keyForTrip(trip.id()), trip));
        return pendingTrips.entrySet().stream().toList();
    }

    private String keyForTrip(String tripID) throws IllegalArgumentException {
        String[] parts = tripID.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid trip ID: " + tripID + "; Expected 5 numbers got " + parts.length);
        }
        return String.join("|", parts[0], parts[1], parts[3]);
    }

    private List<TripDto> parseTrips(String json) {
        var tripsNode = objectMapper.readTree(json).get("trips");
        if (tripsNode == null || tripsNode.isEmpty()) {
            return List.of();
        }
        return objectMapper.convertValue(tripsNode, new TypeReference<>() {});
    }

    private @NonNull Mono<Boolean> fetchAndStore(String tripKey, TripDto tripDto) {
        return tripsClient
                .fetchTrip(tripDto.id())
                .map(this::parseTrip)
                .flatMap(trip -> storeTrip(tripKey, trip))
                .doOnNext(
                        stored -> {
                            if (stored) {
                                synchronized (this) {
                                    pendingTrips.remove(tripKey);
                                }
                            }
                        })
                .onErrorResume(
                        error -> {
                            log.warn("Fetching trips failed for {}", tripKey, error);
                            synchronized (this) {
                                pendingTrips.remove(tripKey);
                            }
                            return Mono.just(false);
                        });
    }

    private @NonNull Mono<Boolean> storeTrip(String tripKey, TripDto tripDto) {
        return tripsStore.storeTrip(tripKey, objectMapper.writeValueAsString(tripDto));
    }

    private TripDto parseTrip(String json) {
        var tripNode = objectMapper.readTree(json).get("trip");
        if (tripNode == null || tripNode.isEmpty()) {
            throw new MissingFormatArgumentException("Missing trip");
        }
        return objectMapper.convertValue(tripNode, new TypeReference<>() {});
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TripDto(String id, List<StopOverDto> stopovers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StopOverDto(StopDto stop) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StopDto(String id, String name) {}
}
