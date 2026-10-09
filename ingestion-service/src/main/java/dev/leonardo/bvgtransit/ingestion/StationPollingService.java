package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StationPollingService {
    private static final Logger log = LoggerFactory.getLogger(StationPollingService.class);

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<StationRepository.StationDTO> stations;

    private final WebClient bvgWebClient;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final PollingProperties pollingProperties;

    public StationPollingService(
            StationRepository stationRepository_,
            WebClient bvgWebClient_,
            ReactiveRedisTemplate<String, String> redisTemplate_,
            PollingProperties pollingProperties_) {
        stations = stationRepository_.getStations();
        bvgWebClient = bvgWebClient_;
        redisTemplate = redisTemplate_;
        pollingProperties = pollingProperties_;
    }

    @Scheduled(fixedRateString = "${ingestion.polling.interval-ms}")
    public void pollDepartures() {
        int batchSize = pollingProperties.batchSize();
        int start = currentIndex.getAndUpdate(x -> (x + batchSize) % stations.size());
        log.debug("Polling {} stations from index {}", batchSize, start);
        Flux.range(0, batchSize)
                .map(i -> stations.get((start + i) % stations.size()))
                .map(StationRepository.StationDTO::id)
                .flatMap(
                        stationID ->
                                fetchAndStore(stationID)
                                        .onErrorResume(
                                                e -> {
                                                    log.error(
                                                            "Polling station {} failed: {}",
                                                            stationID,
                                                            e.getMessage(),
                                                            e);
                                                    return Mono.empty();
                                                }),
                        10)
                .then()
                .subscribe(_ -> {}, error -> log.error("Polling batch of stations failed", error));
    }

    private Mono<Void> fetchAndStore(String stationID) {
        return fetchDepartures(stationID).flatMap(json -> storeDepartures(stationID, json)).then();
    }

    private @NonNull Mono<Boolean> storeDepartures(String stationID, String json) {
        return redisTemplate
                .opsForValue()
                .set("station:" + stationID + ":departures", json, Duration.ofSeconds(180));
    }

    private @NonNull Mono<String> fetchDepartures(String stationID) {
        return bvgWebClient
                .get()
                .uri(
                        "/stops/"
                                + stationID
                                + "/departures?tram=false&bus=false&ferry=false&express=false&regional=false")
                .retrieve()
                .bodyToMono(String.class);
    }

    @ConfigurationProperties("ingestion.polling")
    public record PollingProperties(int batchSize, int intervalMs) {}
}
