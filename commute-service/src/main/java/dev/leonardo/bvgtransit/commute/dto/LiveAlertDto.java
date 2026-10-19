package dev.leonardo.bvgtransit.commute.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record LiveAlertDto(
        Long routeId,
        LocalTime plannedWhen,
        String fromId,
        String toId,
        String directionId,
        String directionName,
        int delay,
        List<UUID> userIds) {}
