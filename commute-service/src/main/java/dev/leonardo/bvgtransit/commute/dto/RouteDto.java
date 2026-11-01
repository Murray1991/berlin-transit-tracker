package dev.leonardo.bvgtransit.commute.dto;

public class RouteDto {
    public record Request(String startStation, String endStation, String departureTime) {}

    public record Response(
            java.util.UUID id, String startStation, String endStation, String departureTime) {}
}
