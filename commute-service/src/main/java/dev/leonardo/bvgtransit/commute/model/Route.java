package dev.leonardo.bvgtransit.commute.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "routes",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_routes_start_end_departure",
                        columnNames = {"start_station_id", "end_station_id", "departure_time"}))
@Getter
@Setter
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id")
    private Station startStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_station_id")
    private Station endStation;

    private LocalTime departureTime;

    @ManyToMany(mappedBy = "savedRoutes")
    private Set<User> users = new HashSet<>();

    public void addUser(User user) {
        users.add(user);
    }
}
