package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;
import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommuteService {

    private final RouteService routeService;
    private final LiveTransitService liveTransitService;
    private final LiveAlertService liveAlertService;

    public CommuteService(
            RouteService routeService_,
            LiveTransitService liveTransitService_,
            LiveAlertService liveAlertService_) {
        routeService = routeService_;
        liveTransitService = liveTransitService_;
        liveAlertService = liveAlertService_;
    }

    @Scheduled(cron = "0 * * * * *")
    public void pollLiveStations() {
        log.info("Polling live stations");
        List<Route> nextRoutes = routeService.getNextRoutes();
        if (nextRoutes.isEmpty()) {
            return;
        }

        log.info("Routes available: {}", nextRoutes.size());
        Map<String, Collection<LiveTransitDto>> liveDepartures =
                liveTransitService.getLiveDepartures(
                        nextRoutes.stream()
                                .map(Route::getStartStation)
                                .map(Station::getId)
                                .collect(Collectors.toSet()));

        log.info("Live departures available: {}", liveDepartures.size());
        for (Route route : nextRoutes) {
            String startStationId = route.getStartStation().getId();
            String stopStationId = route.getEndStation().getId();
            LocalTime departureTime = route.getDepartureTime();
            liveDepartures.get(startStationId).stream()
                    .filter(ltd -> ltd.stopAt(stopStationId))
                    .filter(ltd -> ltd.plannedWhen().isAfter(departureTime))
                    .filter(ltd1 -> ltd1.hasStopAfter(startStationId, stopStationId))
                    .min(Comparator.comparing(LiveTransitDto::plannedWhen))
                    .ifPresent(ltd -> sendLiveAlert(route, ltd, startStationId, stopStationId));
        }
    }

    private void sendLiveAlert(
            Route route, LiveTransitDto ltd, String startStationId, String stopStationId) {
        liveAlertService.sendLiveAlert(
                new LiveAlertDto(
                        route.getId(),
                        ltd.plannedWhen(),
                        startStationId,
                        stopStationId,
                        ltd.destinationId(),
                        ltd.direction(),
                        ltd.delay(),
                        route.getUsers().stream().map(User::getPublicId).toList()));
    }
}
