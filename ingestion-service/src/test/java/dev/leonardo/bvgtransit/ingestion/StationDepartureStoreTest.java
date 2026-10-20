package dev.leonardo.bvgtransit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.leonardo.bvgtransit.ingestion.departures.BvgDeparturesStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import reactor.core.publisher.Mono;

import java.time.Duration;

class StationDepartureStoreTest {

    @Test
    void storeDepartures() {
        ReactiveRedisTemplate<String, String> redisTemplate =
                Mockito.mock(ReactiveRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOperations =
                Mockito.mock(ReactiveValueOperations.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String id = "90000";
        String json = "{\"departures\": [], \"realtimeDataUpdatedAt\": 12345}";
        Duration ttl = Duration.ofSeconds(180);
        var store = new BvgDeparturesStore(redisTemplate);
        Mockito.when(valueOperations.set(store.key(id), json, ttl)).thenReturn(Mono.just(true));

        assertEquals(true, store.storeDepartures(id, json).block());
        Mockito.verify(valueOperations).set(store.key(id), json, ttl);
    }
}
