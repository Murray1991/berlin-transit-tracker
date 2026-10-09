package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

public interface DeparturesClient {
    @NonNull Mono<String> fetchDepartures(String stationID);
}
