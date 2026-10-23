package dev.leonardo.bvgtransit.ingestion.bvg;

import dev.leonardo.bvgtransit.ingestion.departures.DeparturesClient;
import dev.leonardo.bvgtransit.ingestion.trips.TripsClient;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class BvgClient implements DeparturesClient, TripsClient {
    private final WebClient webClient;

    public BvgClient(WebClient webClient_) {
        webClient = webClient_;
    }

    @Override
    public @NonNull Mono<String> fetchDepartures(String stationID) {
        return webClient
                .get()
                .uri(
                        "/stops/"
                                + stationID
                                + "/departures?tram=false&bus=false&ferry=false&express=false&regional=false")
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public @NonNull Mono<String> fetchTrips() {
        return webClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/trips")
                                        .queryParam("tram", false)
                                        .queryParam("bus", false)
                                        .queryParam("ferry", false)
                                        .queryParam("express", false)
                                        .queryParam("regional", false)
                                        .queryParam("stopovers", true)
                                        .queryParam(
                                                "operatorNames",
                                                "Berliner Verkehrsbetriebe,S-Bahn Berlin GmbH")
                                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public @NonNull Mono<String> fetchTrip(String tripID) {
        return webClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/trips/" + tripID)
                                        .queryParam("tram", false)
                                        .queryParam("bus", false)
                                        .queryParam("ferry", false)
                                        .queryParam("express", false)
                                        .queryParam("regional", false)
                                        .queryParam("stopovers", true)
                                        .queryParam(
                                                "operatorNames",
                                                "Berliner Verkehrsbetriebe,S-Bahn Berlin GmbH")
                                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
