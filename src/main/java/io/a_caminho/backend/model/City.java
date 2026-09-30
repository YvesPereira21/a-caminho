package io.a_caminho.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @OneToOne(mappedBy = "city")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @ManyToOne
    @JoinColumn(name = "state_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private State state;

    @OneToMany(mappedBy = "city")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<University> universities = new HashSet<>();
}
