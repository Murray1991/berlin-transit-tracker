package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;

import reactor.core.publisher.Mono;

public interface StationDepartureStore {
    String key(String stationID);

    @NonNull Mono<Boolean> storeDepartures(String stationID, String json);
}
