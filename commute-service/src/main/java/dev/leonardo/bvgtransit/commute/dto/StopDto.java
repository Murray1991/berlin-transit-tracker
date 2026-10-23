package dev.leonardo.bvgtransit.commute.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StopDto(String id, String name) {}
