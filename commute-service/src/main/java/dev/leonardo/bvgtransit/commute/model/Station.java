package dev.leonardo.bvgtransit.commute.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stations")
@Getter
@Setter
public class Station {
    @Id
    private String id;
    private String name;
}
