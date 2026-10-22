package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.leonardo.bvgtransit.ingestion.bvg.BvgRedisStore;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import reactor.core.publisher.Mono;

import java.time.Duration;

class BvgRedisStoreTest {

    @Test
    void storeDepartures() {
        ReactiveRedisTemplate<String, String> redisTemplate =
                Mockito.mock(ReactiveRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOperations =
                Mockito.mock(ReactiveValueOperations.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String id = "90000";
        String json = "{\"departures\": [], \"realtimeDataUpdatedAt\": 12345}";
        String expectedKey = "station:90000:departures";
        Duration ttl = Duration.ofSeconds(180);
        var store = new BvgRedisStore(redisTemplate);
        Mockito.when(valueOperations.set(expectedKey, json, ttl)).thenReturn(Mono.just(true));

        assertEquals(true, store.storeDepartures(id, json).block());
        Mockito.verify(valueOperations).set(expectedKey, json, ttl);
    }

    @Test
    void storeTrip() {
        ReactiveRedisTemplate<String, String> redisTemplate =
                Mockito.mock(ReactiveRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOperations =
                Mockito.mock(ReactiveValueOperations.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String id = "1|68265|0|86|21102026";
        String json = "{\"id\": \"1|68265|0|86|21102026\", \"stopovers\": []}";
        String expectedKey = "trip:1|68265|0|86|21102026";
        Duration ttl = Duration.ofSeconds(180);
        var store = new BvgRedisStore(redisTemplate);
        Mockito.when(valueOperations.set(expectedKey, json, ttl)).thenReturn(Mono.just(true));

        assertEquals(true, store.storeTrip(id, json).block());
        Mockito.verify(valueOperations).set(expectedKey, json, ttl);
    }
}
