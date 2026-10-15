package dev.leonardo.bvgtransit.commute.dto;

import java.time.LocalTime;

public record LiveAlertDto(
        Long routeId,
        LocalTime plannedWhen,
        String fromId,
        String toId,
        String directionId,
        String directionName,
        int delay) {}
