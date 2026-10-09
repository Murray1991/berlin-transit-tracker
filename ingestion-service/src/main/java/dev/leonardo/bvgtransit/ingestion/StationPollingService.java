package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final BvgDeparturesClient bvgDeparturesClient;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final PollingProperties pollingProperties;

    public StationPollingService(
            StationRepository stationRepository_,
            BvgDeparturesClient bvgDeparturesClient_,
            ReactiveRedisTemplate<String, String> redisTemplate_,
            PollingProperties pollingProperties_) {
        stations = stationRepository_.getStations();
        bvgDeparturesClient = bvgDeparturesClient_;
        redisTemplate = redisTemplate_;
        pollingProperties = pollingProperties_;

        if (pollingProperties.batchSize() > stations.size()) {
            throw new IllegalArgumentException(
                    "batchSize = %d greater than total number of stations = %d"
                            .formatted(pollingProperties.batchSize(), stations.size()));
        }
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
        return bvgDeparturesClient
                .fetchDepartures(stationID)
                .flatMap(json -> storeDepartures(stationID, json))
                .then();
    }

    private @NonNull Mono<Boolean> storeDepartures(String stationID, String json) {
        return redisTemplate
                .opsForValue()
                .set("station:" + stationID + ":departures", json, Duration.ofSeconds(180));
    }

    @ConfigurationProperties("ingestion.polling")
    public record PollingProperties(int batchSize, int intervalMs, int maxRequestsPerMinute) {
        public PollingProperties {
            if (batchSize <= 0) {
                throw new IllegalArgumentException("batchSize must be greater than 0");
            }
            if (intervalMs <= 0) {
                throw new IllegalArgumentException("intervalMs must be greater than 0");
            }
            if (maxRequestsPerMinute <= 0) {
                throw new IllegalArgumentException("maxRequestsPerMinute must be greater than 0");
            }

            double expectedRequestsPerMinute = batchSize * (60000.0 / intervalMs);
            if (expectedRequestsPerMinute > maxRequestsPerMinute) {
                throw new IllegalArgumentException(
                        "%.2f expected requests per minute greater than maxRequestsPerMinute = %d"
                                .formatted(expectedRequestsPerMinute, maxRequestsPerMinute));
            }
        }
    }
}
