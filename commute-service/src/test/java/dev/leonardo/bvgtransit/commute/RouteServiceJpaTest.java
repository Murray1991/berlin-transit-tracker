package dev.leonardo.bvgtransit.commute;

import dev.leonardo.bvgtransit.commute.dto.RouteDto;
import dev.leonardo.bvgtransit.commute.model.Route;
import dev.leonardo.bvgtransit.commute.model.Station;
import dev.leonardo.bvgtransit.commute.model.User;
import dev.leonardo.bvgtransit.commute.repository.RouteRepository;
import dev.leonardo.bvgtransit.commute.repository.StationRepository;
import dev.leonardo.bvgtransit.commute.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RouteServiceJpaTest {

    @Container @ServiceConnection
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired private RouteService routeService;
    @Autowired private UserRepository userRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @Transactional
    public void savesRouteWithUser() {
        User user = new User();
        user.setUsername("name");
        user.setEmail("name@email.test");
        userRepository.save(user);

        String stationId1 = "some-id-1";
        Station station1 = new Station();
        station1.setId(stationId1);
        station1.setName("station-1");
        stationRepository.save(station1);

        String stationId2 = "some-id-2";
        Station station2 = new Station();
        station2.setId(stationId2);
        station2.setName("station-2");
        stationRepository.save(station2);

        assertEquals(stationId1, station1.getId());
        assertEquals(stationId2, station2.getId());

        String departureTime = "08:30";
        var response =
                routeService.saveRoute(
                        user.getId(),
                        new RouteDto.Request(station1.getId(), station2.getId(), departureTime));

        // check response
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
}
