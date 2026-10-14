package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommuteService {

    private final RouteService routeService;
    private final LiveTransitService liveTransitService;

    public CommuteService(RouteService routeService_, LiveTransitService liveTransitService_) {
        routeService = routeService_;
        liveTransitService = liveTransitService_;
    }

    @Scheduled(cron = "0 * * * * *")
    private void pollLiveStations() {
        List<Route> nextRoutes = routeService.getNextRoutes();
        if (nextRoutes.isEmpty()) {
            return;
        }

        Map<String, List<LiveTransitDto>> liveDepartures =
                liveTransitService.getLiveDepartures(
                        nextRoutes.stream()
                                .map(Route::getStartStation)
                                .map(Station::getId)
                                .collect(Collectors.toSet()));

        for (Route route : nextRoutes) {
            String startStationId = route.getStartStation().getId();
            String stopStationId = route.getEndStation().getId();
            liveDepartures.get(startStationId).stream()
                    .filter(ltd -> ltd.hasDestination(stopStationId))
                    .filter(ltd -> ltd.plannedWhen().isAfter(route.getDepartureTime()))
                    .min(Comparator.comparing(LiveTransitDto::plannedWhen))
                    .filter(ltd -> ltd.delay() > 0)
                    .ifPresent(
                            ltd ->
                                    log.info(
                                            "Valid transit data (direction {} at {}) for route {} has a delay of {}",
                                            ltd.direction(),
                                            ltd.plannedWhen(),
                                            route.getId(),
                                            ltd.delay()));
        }
    }
}
