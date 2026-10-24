package dev.leonardo.bvgtransit.commute.dto;

import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public record LiveTransitDto(
        String tripId,
        java.time.LocalTime when,
        java.time.LocalTime plannedWhen,
        int delay,
        String direction,
        String lineName,
        String originId,
        String originName,
        String destinationId,
        String destinationName,
        List<StopDto> stopovers) {

    public LiveTransitDto withStopovers(List<StopDto> stopovers) {
        return new LiveTransitDto(
                tripId,
                when,
                plannedWhen,
                delay,
                direction,
                lineName,
                originId,
                originName,
                destinationId,
                destinationName,
                stopovers);
    }

    public boolean stopAt(String stopId) {
        return stopovers.stream().map(StopDto::id).anyMatch(stopId::equals);
    }

    public boolean hasStopAfter(String startId, String stopId) {
        int startIndex =
                IntStream.range(0, stopovers.size())
                        .filter(i -> stopovers.get(i).id().equals(startId))
                        .findFirst()
                        .orElse(-1);
        return startIndex >= 0
                && stopovers.subList(startIndex, stopovers.size()).stream()
                        .anyMatch(stop -> stop.id().equals(stopId));
    }

    public static LiveTransitDto fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        return fromJsonNode(objectMapper.readTree(json));
    }

    public static LiveTransitDto fromJsonNode(JsonNode node) {
        LocalTime when = parseTime(node, "when");
        LocalTime plannedWhen = parseTime(node, "plannedWhen");
        return new LiveTransitDto(
                node.get("tripId").asString(),
                when,
                plannedWhen,
                node.get("delay").asInt(),
                node.get("direction").asString(),
                node.get("line").get("name").asString(),
                Optional.ofNullable(node.get("origin").get("id"))
                        .map(JsonNode::asString)
                        .orElse(null),
                Optional.ofNullable(node.get("origin").get("name"))
                        .map(JsonNode::asString)
                        .orElse(null),
                node.get("destination").get("id").asString(),
                node.get("destination").get("name").asString(),
                List.of());
    }

    private static LocalTime parseTime(JsonNode node, String when) {
        try {
            return Date.from(Instant.parse(node.get(when).asString()))
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
        } catch (DateTimeParseException e) {
            log.error(
                    "Error parsing field {} in {}: {}",
                    when,
                    node.toPrettyString(),
                    e.getMessage());
            return null;
        }
    }
}
