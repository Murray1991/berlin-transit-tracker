package dev.leonardo.bvgtransit.ingestion.bvg;

import dev.leonardo.bvgtransit.ingestion.departures.DeparturesClient;
import dev.leonardo.bvgtransit.ingestion.trips.TripsClient;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@Component
public class BvgClient implements DeparturesClient, TripsClient {

    private static final Logger log = LoggerFactory.getLogger(BvgClient.class);

    private final WebClient webClient;

    public BvgClient(WebClient webClient_) {
        webClient = webClient_;
    }

    @Override
    public @NonNull Mono<String> fetchDepartures(String stationID) {
        return webClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/stops/" + stationID + "/departures")
                                        .queryParam("duration", 40)
                                        .queryParam("tram", false)
                                        .queryParam("bus", false)
                                        .queryParam("ferry", false)
                                        .queryParam("express", false)
                                        .queryParam("regional", false)
                                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(
                        error -> {
                            if (error instanceof WebClientResponseException responseError) {
                                log.error(
                                        "BVG request failed: GET /stops/{}/departures returned {}. Response: {}",
                                        stationID,
                                        responseError.getStatusCode(),
                                        responseError.getResponseBodyAsString());
                            } else {
                                log.error("BVG request failed for station {}", stationID, error);
                            }
                        });
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
                .bodyToMono(String.class)
                .doOnError(
                        error -> {
                            if (error instanceof WebClientResponseException responseError) {
                                log.error(
                                        "BVG request failed: GET /trips/ returned {}. Response: {}",
                                        responseError.getStatusCode(),
                                        responseError.getResponseBodyAsString());
                            } else {
                                log.error("BVG request failed for trips", error);
                            }
                        });
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
                .bodyToMono(String.class)
                .doOnError(
                        error -> {
                            if (error instanceof WebClientResponseException responseError) {
                                log.error(
                                        "BVG request failed: GET /trips/{} returned {}. Response: {}",
                                        tripID,
                                        responseError.getStatusCode(),
                                        responseError.getResponseBodyAsString());
                            } else {
                                log.error("BVG request failed for trip {}", tripID, error);
                            }
                        });
    }
}
