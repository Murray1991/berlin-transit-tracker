package dev.leonardo.bvgtransit.commute.dto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public record LiveTransitDto(
        String tripId,
        java.time.LocalTime when,
        java.time.LocalTime plannedWhen,
        int delay,
        String direction,
        String destination) {
    public boolean includeStation(String stationId) {
        // TODO LiveTransitDto should contain a list of stops
        return stationId.equals(destination);
    }

    public static LiveTransitDto fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(json);
        LocalTime when =
                Date.from(Instant.parse(node.get("when").asString()))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime();
        LocalTime plannedWhen =
                Date.from(Instant.parse(node.get("when").asString()))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime();
        return new LiveTransitDto(
                node.get("tripId").asString(),
                when,
                plannedWhen,
                node.get("delay").asInt(),
                node.get("direction").asString(),
                node.get("destination").get("id").asString());
    }
}
