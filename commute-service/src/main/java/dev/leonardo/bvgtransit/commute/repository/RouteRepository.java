package dev.leonardo.bvgtransit.commute.repository;

import dev.leonardo.bvgtransit.commute.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByDepartureDateBetween(LocalTime from, LocalTime to);
}
