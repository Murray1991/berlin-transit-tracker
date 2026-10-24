package dev.leonardo.bvgtransit.commute.service;

import dev.leonardo.bvgtransit.commute.dto.LiveTransitDto;
import dev.leonardo.bvgtransit.commute.dto.StopDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;

import org.jspecify.annotations.NonNull;
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
        LiveTransitDto ltd = createLiveTransitData(time, route, 120);

        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.singletonList(route));
        Mockito.when(liveTransitService.getLiveDepartures(Set.of("station-1")))
                .thenReturn(Map.of("station-1", List.of(ltd)));
        Mockito.when(liveTransitService.isAlertable(Mockito.any(), Mockito.any())).thenReturn(true);
        commuteService.pollLiveStations();

        Mockito.verify(liveAlertService, Mockito.times(1))
                .sendLiveAlert(CommuteService.createLiveAlertDto(route, ltd));
    }

    @Test
    void doesNotSendAlertForNonDelayedTrain() {
        LocalTime time = LocalTime.now();
        Route route = createRoute("station-1", "station-2", time);
        LiveTransitDto ltd = createLiveTransitData(time, route, 0);

        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.singletonList(route));
        Mockito.when(liveTransitService.getLiveDepartures(Set.of("station-1")))
                .thenReturn(Map.of("station-1", List.of(ltd)));

        commuteService.pollLiveStations();

        Mockito.verifyNoInteractions(liveAlertService);
    }

    @Test
    void noInteractions() {
        Mockito.when(routeService.getNextRoutes()).thenReturn(Collections.emptyList());
        commuteService.pollLiveStations();
        Mockito.verifyNoInteractions(liveTransitService, liveAlertService);
    }

    public static Route createRoute(String stationId1, String stationId2, LocalTime time) {
        Station s1 = new Station();
        s1.setId(stationId1);
        s1.setName("name" + stationId1);

        Station s2 = new Station();
        s2.setId(stationId2);
        s2.setId("name" + stationId2);

        Route route = new Route();
        route.setId(11L);
        route.setStartStation(s1);
        route.setEndStation(s2);

        route.setDepartureTime(time);
        route.addUser(new User());

        return route;
    }

    public static @NonNull LiveTransitDto createLiveTransitData(
            LocalTime time, Route route, int delay) {
        return new LiveTransitDto(
                "trip-1",
                time.plusMinutes(1),
                time.plusMinutes(1),
                delay,
                "Direction",
                "S9",
                route.getStartStation().getId(),
                route.getStartStation().getName(),
                route.getEndStation().getId(),
                route.getEndStation().getName(),
                List.of(
                        new StopDto(
                                route.getStartStation().getId(), route.getStartStation().getName()),
                        new StopDto(
                                route.getEndStation().getId(), route.getEndStation().getName())));
    }
}
