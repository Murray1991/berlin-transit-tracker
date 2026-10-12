package dev.leonardo.bvgtransit.commute.dto;

import java.time.LocalDateTime;

public record ErrorDto(LocalDateTime timestamp, int status, String errorCode, String errorDescription) {
}
