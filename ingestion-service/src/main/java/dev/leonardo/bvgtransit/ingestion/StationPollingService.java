package dev.leonardo.bvgtransit.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StationPollingService {
    private static final Logger log = LoggerFactory.getLogger(StationPollingService.class);

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<StationRepository.StationDTO> stations;
    private final Integer BATCH_SIZE = 25;

    private final WebClient bvgWebClient;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public StationPollingService(
            StationRepository stationRepository_,
            WebClient bvgWebClient_,
            ReactiveRedisTemplate<String, String> redisTemplate_) {
        stations = stationRepository_.getStations();
        bvgWebClient = bvgWebClient_;
        redisTemplate = redisTemplate_;
    }

    @Scheduled(fixedRate = 15000)
    public void pollDepartures() {
        int start = currentIndex.getAndUpdate(x -> (x + BATCH_SIZE) % stations.size());
        log.info("Polling {} stations from index {}", BATCH_SIZE, start);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int index = (start + i) % stations.size();
            var station = stations.get(index);
            bvgWebClient
                    .get()
                    .uri(
                            "/stops/"
                                    + station.id()
                                    + "/departures?tram=false&bus=false&ferry=false&express=false&regional=false")
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(
                            json ->
                                    redisTemplate
                                            .opsForValue()
                                            .set(
                                                    "station:" + station.id() + ":departures",
                                                    json,
                                                    Duration.ofSeconds(180)))
                    .subscribe(
                            _ -> log.info("Stored departures for station {}", station.id()),
                            error -> log.error("Polling station {} failed", station.id(), error));
        }
    }
}
