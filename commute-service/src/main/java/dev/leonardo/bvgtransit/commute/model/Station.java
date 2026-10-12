package dev.leonardo.bvgtransit.commute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stations")
public class Station {
    @Id
    private String id;
    private String name;
}
