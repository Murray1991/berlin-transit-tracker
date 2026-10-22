package dev.leonardo.bvgtransit.ingestion.departures;

import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BvgDeparturesStore implements DeparturesStore {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public BvgDeparturesStore(ReactiveRedisTemplate<String, String> redisTemplate_) {
        redisTemplate = redisTemplate_;
    }

    @Override
    public @NonNull Mono<Boolean> storeDepartures(String stationID, String json) {
        return redisTemplate
                .opsForValue()
                .set(keyForStation(stationID), json, Duration.ofSeconds(180));
    }

    private String keyForStation(String stationID) {
        return "station:" + stationID + ":departures";
    }
}
