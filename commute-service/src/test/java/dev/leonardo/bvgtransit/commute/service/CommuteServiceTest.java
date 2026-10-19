package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveAlertDto;
import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class CommuteServiceTest {

    @Mock RouteService routeService;
    @Mock LiveTransitService liveTransitService;
    @Mock LiveAlertService liveAlertService;

    @InjectMocks CommuteService commuteService;

    @Test
    void sendAlertForDelayedTrain() {
        LocalTime time = LocalTime.now();
        Route route = createRoute("station-1", "station-2", time);
        LiveTransitDto delayedDeparture =
                new LiveTransitDto(
                        "trip-1",
                        time.plusMinutes(1),
                        time.plusMinutes(1),
                        120,
                        "Direction",
                        route.getEndStation().getId());

        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.singletonList(route));
        Mockito.when(liveTransitService.getLiveDepartures(Set.of("station-1")))
                .thenReturn(Map.of("station-1", List.of(delayedDeparture)));

        commuteService.pollLiveStations();

        Mockito.verify(liveAlertService, Mockito.times(1))
                .sendDelayAlert(
                        new LiveAlertDto(
                                route.getId(),
                                delayedDeparture.plannedWhen(),
                                route.getStartStation().getId(),
                                route.getEndStation().getId(),
                                delayedDeparture.destinationId(),
                                delayedDeparture.direction(),
                                delayedDeparture.delay(),
                                route.getUsers().stream().map(User::getPublicId).toList()));
    }

    @Test
    void doesNotSendAlertForNonDelayedTrain() {
        LocalTime time = LocalTime.now();
        Route route = createRoute("station-1", "station-2", time);
        LiveTransitDto delayedDeparture =
                new LiveTransitDto(
                        "trip-1",
                        time.plusMinutes(1),
                        time.plusMinutes(1),
                        0,
                        "Direction",
                        route.getEndStation().getId());

        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.singletonList(route));
        Mockito.when(liveTransitService.getLiveDepartures(Set.of("station-1")))
                .thenReturn(Map.of("station-1", List.of(delayedDeparture)));

        commuteService.pollLiveStations();

        Mockito.verifyNoInteractions(liveAlertService);
    }

    @Test
    void noInteractions() {
        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.emptyList());
        commuteService.pollLiveStations();
        Mockito.verifyNoInteractions(liveTransitService, liveAlertService);
    }

    private Route createRoute(String stationId1, String stationId2, LocalTime time) {
        Station s1 = new Station();
        s1.setId(stationId1);

        Station s2 = new Station();
        s2.setId(stationId2);

        Route route = new Route();
        route.setStartStation(s1);
        route.setEndStation(s2);

        route.setDepartureTime(time);
        route.addUser(new User());

        return route;
    }
}
