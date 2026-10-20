package dev.leonardo.bvgtransit.commute.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    @Mock private UserRepository users;
    @Mock private RouteRepository routes;
    @Mock private StationRepository stations;

    @InjectMocks private RouteService routeService;

    private static @NonNull UUID getPublicId() {
        return UUID.randomUUID();
    }

    @Test
    void acceptValidInput() {
        var user = new User();

        var station1 = new Station();
        station1.setId("station-1");

        var station2 = new Station();
        station2.setId("station-2");

        UUID publicId = getPublicId();
        Mockito.when(users.findByPublicId(publicId)).thenReturn(Optional.of(user));
        Mockito.when(stations.findById(station1.getId())).thenReturn(Optional.of(station1));
        Mockito.when(stations.findById(station2.getId())).thenReturn(Optional.of(station2));
        Mockito.when(routes.save(Mockito.any(Route.class)))
                .thenAnswer(
                        i -> {
                            Route route = i.getArgument(0);
                            route.setId(42L);
                            return route;
                        });

        var request = new RouteDto.Request(station1.getId(), station2.getId(), "08:30");
        var response = routeService.saveRoute(publicId, request);
        assertNotNull(response);
        assertEquals(42L, response.id());
        assertEquals("station-1", response.startStation());
        assertEquals("station-2", response.endStation());
        assertEquals("08:30", response.departureTime());
    }

    @Test
    void rejectSameStartAndEnd() {
        UUID publicId = getPublicId();
        var request = new RouteDto.Request("station-1", "station-1", "08:30");
        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> routeService.saveRoute(publicId, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        Mockito.verifyNoInteractions(users, routes, stations);
    }

    @Test
    void rejectWrongDepartureTime() {
        UUID publicId = getPublicId();
        var request = new RouteDto.Request("station-1", "station-2", "bad-time");
        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> routeService.saveRoute(publicId, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        Mockito.verifyNoInteractions(users, routes, stations);
    }

    @Test
    void rejectUnknownDepartureTime() {
        UUID publicId = getPublicId();
        Mockito.when(users.findByPublicId(publicId)).thenReturn(Optional.of(new User()));
        Mockito.when(stations.findById("missing")).thenReturn(Optional.empty());

        var request = new RouteDto.Request("missing", "station-2", "09:45");
        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> routeService.saveRoute(publicId, request));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        Mockito.verify(routes, Mockito.never()).save(Mockito.any());
    }
}
