package dev.leonardo.bvgtransit.commute.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "user_routes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "route_id"))
    private Set<Route> savedRoutes = new HashSet<>();

    public void addRoute(Route newRoute) {
        savedRoutes.add(newRoute);
    }
}
