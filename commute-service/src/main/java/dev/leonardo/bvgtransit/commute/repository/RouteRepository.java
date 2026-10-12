package dev.leonardo.bvgtransit.commute.repository;

import dev.leonardo.bvgtransit.commute.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
}
