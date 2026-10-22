package dev.leonardo.bvgtransit.ingestion.trips;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class TripsPollingService {

    private static final Logger log = LoggerFactory.getLogger(TripsPollingService.class);
    private final TripsClient tripsClient;
    private final TripsStore tripsStore;
    private final ObjectMapper objectMapper;

    public TripsPollingService(
            TripsClient tripsClient_, TripsStore tripsStore_, ObjectMapper objectMapper_) {
        tripsClient = tripsClient_;
        tripsStore = tripsStore_;
        objectMapper = objectMapper_;
    }

    @Scheduled(fixedRate = 90000)
    public void pollTrips() {
        log.debug("Polling trips");
        pollNextTrips()
                .then()
                .subscribe(_ -> {}, error -> log.error("Storing trips failed", error));
    }

    public @NonNull Flux<Boolean> pollNextTrips() {
        return tripsClient
                .fetchTrips()
                .flatMap(json -> Mono.just(parseTrips(json)))
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::storeTrip);
    }

    private List<TripDto> parseTrips(String json) {
        var tripsNode = objectMapper.readTree(json).get("trips");
        if (tripsNode == null || tripsNode.isEmpty()) {
            return List.of();
        }
        return objectMapper.convertValue(tripsNode, new TypeReference<>() {});
    }

    private Mono<Boolean> storeTrip(TripDto tripDto) {
        log.debug("Storing trip {} {}", tripDto.id, tripDto);
        return tripsStore.storeTrip(tripDto.id, objectMapper.writeValueAsString(tripDto));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TripDto(String id, List<StopOverDto> stopovers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StopOverDto(StopDto stop) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StopDto(String id, String name) {}
}
