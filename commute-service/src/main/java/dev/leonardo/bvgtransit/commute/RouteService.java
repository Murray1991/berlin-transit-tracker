package dev.leonardo.bvgtransit.commute;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
public class RouteService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    public RouteService(
            UserRepository userRepository_,
            RouteRepository routeRepository_,
            StationRepository stationRepository_) {
        userRepository = userRepository_;
        routeRepository = routeRepository_;
        stationRepository = stationRepository_;
    }

    @Transactional
    public RouteDto.Response saveRoute(Long userId, RouteDto.Request request) {
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

        try {
            LocalTime.parse(request.departureTime());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid departure time. Must be in the format HH:MM");
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "User not found"));
        Station startStation =
                stationRepository
                        .findById(request.startStation())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Station not found"));
        Station endStation =
                stationRepository
                        .findById(request.endStation())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Station not found"));

        Route route = new Route();
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setDepartureTime(request.departureTime());
        route.addUser(user);
        user.addRoute(route);

        Route saved = routeRepository.save(route);
        return new RouteDto.Response(
                saved.getId(),
                saved.getStartStation().getId(),
                saved.getEndStation().getId(),
                saved.getDepartureTime());
    }
}
