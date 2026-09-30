package io.a_caminho.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bus_id")
    private UUID busId;

    @Column(name = "bus_name", nullable = false)
    private String busName;

    @Column(name = "seats_quantity")
    private Integer seatsQuantity;

    @ManyToOne
    @JoinColumn(name = "municipality_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "route_bus",
        joinColumns = @JoinColumn(name = "bus_id"),
        inverseJoinColumns = @JoinColumn(name = "university_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<University> defaultUniversities = new HashSet<>();

    @ManyToMany(mappedBy = "assignedBuses")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Poll> polls = new HashSet<>();

    @OneToMany(mappedBy = "bus")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Travel> travels = new HashSet<>();
}
