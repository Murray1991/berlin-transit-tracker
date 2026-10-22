package dev.leonardo.bvgtransit.ingestion.trips;

import org.jspecify.annotations.NonNull;

import reactor.core.publisher.Mono;

public interface TripsClient {
    @NonNull Mono<String> fetchTrips();

    @NonNull Mono<String> fetchTrip(String tripID);
}
