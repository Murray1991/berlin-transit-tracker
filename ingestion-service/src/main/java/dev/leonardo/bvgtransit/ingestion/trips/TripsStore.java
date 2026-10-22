package dev.leonardo.bvgtransit.ingestion.trips;

import org.jspecify.annotations.NonNull;

import reactor.core.publisher.Mono;

public interface TripsStore {
    @NonNull Mono<Boolean> storeTrip(String tripID, String json);
}
