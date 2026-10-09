package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class BvgDeparturesClient {
    private final WebClient webClient;

    public BvgDeparturesClient(WebClient webClient_) {
        webClient = webClient_;
    }

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
}
