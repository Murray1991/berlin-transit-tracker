package dev.leonardo.bvgtransit.commute.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.dto.StopDto;

import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LiveTransitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public LiveTransitService(
            ObjectMapper objectMapper_, RedisTemplate<String, String> redisTemplate_) {
        objectMapper = objectMapper_;
        redisTemplate = redisTemplate_;
    }

    public Map<String, Collection<LiveTransitDto>> getLiveDepartures(
            Collection<String> stationIds) {
        Map<String, Collection<LiveTransitDto>> liveDepartures = new HashMap<>();
        for (String stationId : stationIds) {
            List<LiveTransitDto> liveTransits = getLiveDepartures(stationId);
            liveDepartures.put(stationId, withStopovers(liveTransits));
        }
        return liveDepartures;
    }

    private Collection<LiveTransitDto> withStopovers(Collection<LiveTransitDto> liveTransits) {
        if (liveTransits == null || liveTransits.isEmpty()) {
            return Collections.emptyList();
        }

        var keys =
                liveTransits.stream()
                        .map(LiveTransitDto::tripId)
                        .map(this::tripKey)
                        .map(this::keyForTrip)
                        .map("trip:"::concat)
                        .collect(Collectors.toSet());

        Map<String, TripDto> map =
                redisTemplate.opsForValue().multiGet(keys).stream()
                        .filter(Objects::nonNull)
                        .map(this::parseTrip)
                        .collect(Collectors.toMap(trip -> keyForTrip(trip.id()), trip -> trip));

        return liveTransits.stream().map(ltd -> ltd.withStopovers(getStopovers(ltd, map))).toList();
    }

    private @NonNull List<StopDto> getStopovers(LiveTransitDto ltd, Map<String, TripDto> map) {
        return Optional.ofNullable(map.get(keyForTrip(ltd.tripId())))
                .map(TripDto::stopovers)
                .orElse(List.of())
                .stream()
                .map(StopOverDto::stop)
                .toList();
    }

    private String keyForTrip(String tripID) throws IllegalArgumentException {
        String[] parts = tripID.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid trip ID: " + tripID + "; Expected 5 numbers got " + parts.length);
        }
        return String.join("|", parts[0], parts[1], parts[3]);
    }

    private TripDto parseTrip(String json) {
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    private String tripKey(String tripID) {
        String[] parts = tripID.split("\\|", -1);
        if (parts.length != 5) {
            return tripID;
        }
        return String.join("|", parts[0], parts[1], "%num", parts[3], "%dd%mm%yyyy");
    }

    private List<LiveTransitDto> getLiveDepartures(String stationId) {

        List<LiveTransitDto> departures = new ArrayList<>();

        String redisKey = "station:" + stationId + ":departures";
        String json = redisTemplate.opsForValue().get(redisKey);
        if (json == null) {
            return List.of();
        }

        JsonNode nodeDepartures = objectMapper.readTree(json).get("departures");
        for (JsonNode node : nodeDepartures) {
            departures.add(LiveTransitDto.fromJsonNode(node));
        }

        return departures;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TripDto(String id, List<StopOverDto> stopovers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StopOverDto(StopDto stop) {}
}
