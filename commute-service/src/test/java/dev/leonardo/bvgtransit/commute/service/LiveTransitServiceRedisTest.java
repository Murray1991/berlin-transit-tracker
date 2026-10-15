package dev.leonardo.bvgtransit.commute.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.List;

@DataRedisTest
@Testcontainers
@Import({LiveTransitService.class})
class LiveTransitServiceRedisTest {
    @Container
    @ServiceConnection(name = "redis")
    private static final GenericContainer<?> redis =
            new GenericContainer<>("redis:8.10.2").withExposedPorts(6379);

    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired LiveTransitService liveTransitService;

    @Test
    void readsDeparturesForStation() {
        String stationId = "900100003";
        String redisKey = "station:" + stationId + ":departures";
        String redisData =
                """
                {
                  "departures": [{
                    "tripId": "trip-1",
                    "when": "2026-10-15T08:31:00+02:00",
                    "plannedWhen": "2026-10-15T08:30:00+02:00",
                    "delay": 1,
                    "direction": "Potsdam",
                    "destination": { "id": "900230999" }
                  }]
                }
                """;
        redisTemplate.opsForValue().set(redisKey, redisData);

        var departures = liveTransitService.getLiveDepartures(List.of(stationId)).get(stationId);
        assertEquals(1, departures.size());

        LiveTransitDto departure = departures.getFirst();
        assertEquals("trip-1", departure.tripId());
        assertEquals(LocalTime.of(8, 31), departure.when());
        assertEquals(LocalTime.of(8, 30), departure.plannedWhen());
        assertEquals(1, departure.delay());
        assertEquals("Potsdam", departure.direction());
        assertEquals("900230999", departure.destinationId());
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class MapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
