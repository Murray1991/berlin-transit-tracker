package dev.leonardo.bvgtransit.commute.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record LiveAlertDto(
        Long routeId,
        String stopStartId,
        String stopStartName,
        String stopEndId,
        String stopEndName,
        String originId,
        String originName,
        String destinationId,
        String destinationName,
        String direction,
        String lineName,
        LocalTime plannedWhen,
        LocalTime when,
        int delay,
        List<UUID> userIds) {}
