package dev.leonardo.bvgtransit.commute.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@Testcontainers
public class RouteServiceJpaTest {

    @Container @ServiceConnection
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @MockitoBean StationService.StationCatalogClient catalogClient;

    @Autowired private RouteService routeService;
    @Autowired private UserRepository userRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @Transactional
    public void savesRouteWithUser() {
        User user = saveUser("name");

        String stationId1 = "station1";
        String stationId2 = "station2";

        Station station1 = saveStation(stationId1);
        Station station2 = saveStation(stationId2);
        assertEquals(stationId1, station1.getId());
        assertEquals(stationId2, station2.getId());

        String departureTime = "08:30";
        var response = saveRouteForUser(user, station1, station2, departureTime);
        assertNotNull(response);
        assertEquals(stationId1, response.startStation());
        assertEquals(stationId2, response.endStation());
        assertEquals(departureTime, response.departureTime());

        // synchronize and detach persistence context
        entityManager.flush();
        entityManager.clear();

        // check db
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        Route savedRoute = routeRepository.findById(response.id()).orElseThrow();
        assertNotNull(savedRoute.getId());
        assertEquals(stationId1, savedRoute.getStartStation().getId());
        assertEquals(stationId2, savedRoute.getEndStation().getId());
        assertEquals(1, savedUser.getSavedRoutes().size());
        assertEquals(1, savedRoute.getUsers().size());
    }

    @Test
    @Transactional
    public void returnsNextRoutes() {
        User user1 = saveUser("name1");
        User user2 = saveUser("name2");

        Station station1 = saveStation("station1");
        Station station2 = saveStation("station2");
        Station station3 = saveStation("station3");
        Station station4 = saveStation("station4");

        LocalTime from = LocalTime.now();
        var resp1 = saveRouteForUser(user1, station1, station2, from.plusMinutes(10).toString());
        var resp2 = saveRouteForUser(user2, station1, station3, from.plusMinutes(27).toString());
        var resp3 = saveRouteForUser(user1, station1, station4, from.plusMinutes(32).toString());

        // synchronize and detach persistence context
        entityManager.flush();
        entityManager.clear();

        List<Route> nextRoutes = routeService.getNextRoutes();
        assertNotNull(nextRoutes);
        assertEquals(2, nextRoutes.size());
        assertEquals(resp1.id(), nextRoutes.get(0).getId());
        assertEquals(resp2.id(), nextRoutes.get(1).getId());

        Route route3 = routeRepository.findById(resp3.id()).orElseThrow();
        assertEquals(resp3.id(), route3.getId());
    }

    private RouteDto.Response saveRouteForUser(
            User user1, Station station1, Station station2, String from) {
        return routeService.saveRoute(
                user1.getPublicId(),
                new RouteDto.Request(station1.getId(), station2.getId(), from));
    }

    private @NonNull User saveUser(String name) {
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@email.test");
        return userRepository.save(user);
    }

    private @NonNull Station saveStation(String stationId) {
        Station station = new Station();
        station.setId(stationId);
        station.setName(stationId + "-name");
        return stationRepository.save(station);
    }
}
