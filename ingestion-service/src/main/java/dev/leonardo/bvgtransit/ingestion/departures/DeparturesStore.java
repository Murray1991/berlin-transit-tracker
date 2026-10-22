package dev.leonardo.bvgtransit.ingestion.departures;

import org.jspecify.annotations.NonNull;

import reactor.core.publisher.Mono;

public interface DeparturesStore {
    @NonNull Mono<Boolean> storeDepartures(String stationID, String json);
}
