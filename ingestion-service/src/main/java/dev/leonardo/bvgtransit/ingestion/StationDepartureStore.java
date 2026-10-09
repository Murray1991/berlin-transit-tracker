package dev.leonardo.bvgtransit.ingestion;

import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class StationDepartureStore {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public StationDepartureStore(ReactiveRedisTemplate<String, String> redisTemplate_) {
        redisTemplate = redisTemplate_;
    }

    public @NonNull Mono<Boolean> storeDepartures(String stationID, String json) {
        return redisTemplate.opsForValue().set(key(stationID), json, Duration.ofSeconds(180));
    }

    public static String key(String stationID) {
        return "station:" + stationID + ":departures";
    }
}
