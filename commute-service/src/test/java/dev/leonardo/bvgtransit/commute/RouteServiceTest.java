package dev.leonardo.bvgtransit.commute;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;

import dev.leonardo.bvgtransit.commute.service.RouteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    @Mock private UserRepository users;
    @Mock private RouteRepository routes;
    @Mock private StationRepository stations;

    @InjectMocks
    private RouteService routeService;

    @Test
    void rejectSameStartAndEnd() {
        var request = new RouteDto.Request("station-1", "station-1", "08:30");
        var error = assertThrows(ResponseStatusException.class, () -> routeService.saveRoute(1L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        Mockito.verifyNoInteractions(users, routes, stations);
    }

    @Test
    void rejectWrongDepartureTime() {
        var request = new RouteDto.Request("station-1", "station-2", "bad-time");
        var error = assertThrows(ResponseStatusException.class, () -> routeService.saveRoute(1L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        Mockito.verifyNoInteractions(users, routes, stations);
    }

    @Test
    void rejectUnknownDepartureTime() {
        Mockito.when(users.findById(1L)).thenReturn(Optional.of(new User()));
        Mockito.when(stations.findById("missing")).thenReturn(Optional.empty());

        var request = new RouteDto.Request("missing", "station-2", "09:45");
        var error = assertThrows(ResponseStatusException.class, () -> routeService.saveRoute(1L, request));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        Mockito.verify(routes, Mockito.never()).save(Mockito.any());
    }
}
