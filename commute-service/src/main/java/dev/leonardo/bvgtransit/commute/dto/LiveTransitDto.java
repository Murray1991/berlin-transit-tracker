package dev.leonardo.bvgtransit.commute.dto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public record LiveTransitDto(
        String tripId,
        java.time.LocalTime when,
        java.time.LocalTime plannedWhen,
        int delay,
        String direction,
        String destinationId,
        List<StopDto> stopovers) {

    public LiveTransitDto withStopovers(List<StopDto> stopovers) {
        return new LiveTransitDto(
                tripId, when, plannedWhen, delay, direction, destinationId, stopovers);
    }

    public boolean hasDestination(String stationId) {
        return stationId.equals(destinationId);
    }

    public static LiveTransitDto fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        return fromJsonNode(objectMapper.readTree(json));
    }

    public static LiveTransitDto fromJsonNode(JsonNode node) {
        LocalTime when =
                Date.from(Instant.parse(node.get("when").asString()))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime();
        LocalTime plannedWhen =
                Date.from(Instant.parse(node.get("plannedWhen").asString()))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime();
        return new LiveTransitDto(
                node.get("tripId").asString(),
                when,
                plannedWhen,
                node.get("delay").asInt(),
                node.get("direction").asString(),
                node.get("destination").get("id").asString(),
                List.of());
    }
}
