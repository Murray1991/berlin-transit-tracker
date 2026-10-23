package dev.leonardo.bvgtransit.ingestion.bvg;

import dev.leonardo.bvgtransit.ingestion.departures.DeparturesStore;
import dev.leonardo.bvgtransit.ingestion.trips.TripsStore;

import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BvgRedisStore implements DeparturesStore, TripsStore {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public BvgRedisStore(ReactiveRedisTemplate<String, String> redisTemplate_) {
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

    @Override
    public @NonNull Mono<Boolean> storeTrip(String tripID, String json) {
        return redisTemplate.opsForValue().set(keyForTrip(tripID), json, Duration.ofDays(1));
    }

    @Override
    public @NonNull Mono<Boolean> containsTrip(String tripID) {
        return redisTemplate.hasKey(keyForTrip(tripID));
    }

    private String keyForTrip(String tripID) {
        return "trip:" + tripID;
    }
}
