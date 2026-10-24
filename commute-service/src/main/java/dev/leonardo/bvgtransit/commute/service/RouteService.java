package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;

import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RouteService {

    private final UserService userService;
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    public RouteService(
            UserService userService_,
            RouteRepository routeRepository_,
            StationRepository stationRepository_) {
        userService = userService_;
        routeRepository = routeRepository_;
        stationRepository = stationRepository_;
    }

    @Transactional
    public RouteDto.Response saveRoute(UUID publicUserId, RouteDto.Request request) {
        if (request == null
                || request.startStation() == null
                || request.endStation() == null
                || request.departureTime() == null
                || request.startStation().isBlank()
                || request.endStation().isBlank()
                || request.departureTime().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route fields are required");
        }

        if (request.startStation().equals(request.endStation())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Start and end stations must be different");
        }

        LocalTime departureTime;
        try {
            departureTime = LocalTime.parse(request.departureTime());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid departure time. Must be in the format HH:MM");
        }

        Station startStation =
                stationRepository
                        .findById(request.startStation())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                String.format(
                                                        "Station %s not found",
                                                        request.startStation())));
        Station endStation =
                stationRepository
                        .findById(request.endStation())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                String.format(
                                                        "Station %s not found",
                                                        request.endStation())));

        Route route = new Route();
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setDepartureTime(departureTime);

        userService.addRoute(publicUserId, route);

        Route saved = routeRepository.save(route);
        return new RouteDto.Response(
                saved.getId(),
                saved.getStartStation().getId(),
                saved.getEndStation().getId(),
                saved.getDepartureTime().toString());
    }

    @Transactional
    public List<Route> getNextRoutes() {
        LocalTime now = LocalTime.now();
        LocalTime from = now.minusMinutes(30);
        LocalTime to = now.plusMinutes(30);

        log.info("Fetching routes from {} to {}", from, to);
        if (from.isBefore(to)) {
            return routeRepository.findByDepartureTimeBetween(from, to);
        }

        List<Route> routes = routeRepository.findByDepartureTimeBetween(from, LocalTime.MAX);
        routes.addAll(routeRepository.findByDepartureTimeBetween(LocalTime.MIDNIGHT, to));
        return routes;
    }
}
