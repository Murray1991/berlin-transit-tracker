package dev.leonardo.bvgtransit.ingestion.trips;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class TripsPollingService {

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
        pollNextTrips().then();
    }

    public @NonNull Flux<Boolean> pollNextTrips() {
        return tripsClient
                .fetchTrips()
                .flatMap(json -> Mono.just(parseTrips(json)))
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::storeTrip);
    }

    private List<TripDto> parseTrips(String json) {
        Map<String, List<TripDto>> res = objectMapper.readValue(json, new TypeReference<>() {});
        return res.getOrDefault("trips", List.of());
    }

    private Mono<Boolean> storeTrip(TripDto tripDto) {
        return tripsStore.storeTrip(tripDto.id, objectMapper.writeValueAsString(tripDto));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TripDto(String id, List<StopOverDto> stopovers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StopOverDto(StopDto stop) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StopDto(String id, String name) {}
}
