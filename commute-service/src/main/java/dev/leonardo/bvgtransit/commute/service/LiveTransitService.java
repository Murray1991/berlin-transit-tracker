package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class LiveTransitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public LiveTransitService(
            ObjectMapper objectMapper_, RedisTemplate<String, String> redisTemplate_) {
        objectMapper = objectMapper_;
        redisTemplate = redisTemplate_;
    }

    public Map<String, List<LiveTransitDto>> getLiveDepartures(Collection<String> stationIds) {
        Map<String, List<LiveTransitDto>> liveDepartures = new HashMap<>();
        for (String stationId : stationIds) {
            liveDepartures.put(stationId, getLiveDepartures(stationId));
        }
        return liveDepartures;
    }

    private List<LiveTransitDto> getLiveDepartures(String stationId) {
        String redisKey = "station:" + stationId + ":departures";

        List<LiveTransitDto> departures = new ArrayList<>();

        String json = redisTemplate.opsForValue().get(redisKey);
        JsonNode nodeDepartures = objectMapper.readTree(json).get("departures");
        for (JsonNode node : nodeDepartures) {
            departures.add(LiveTransitDto.fromJsonNode(node));
        }

        return departures;
    }
}
