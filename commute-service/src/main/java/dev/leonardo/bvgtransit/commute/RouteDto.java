package dev.leonardo.bvgtransit.commute;

public class RouteDto {
    public record Request(String startStation, String endStation, String departureTime) {}

    public record Response(Long id, String startStation, String endStation, String departureTime) {}
}
