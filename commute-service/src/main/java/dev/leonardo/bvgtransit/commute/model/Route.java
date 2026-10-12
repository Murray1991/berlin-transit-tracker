package dev.leonardo.bvgtransit.commute.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id")
    private Station startStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_station_id")
    private Station endStation;

    private String departureTime;

    @ManyToMany(mappedBy = "savedRoutes")
    private Set<User> users;
}
