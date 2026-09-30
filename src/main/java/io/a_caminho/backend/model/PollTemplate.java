package io.a_caminho.backend.model;

import io.a_caminho.backend.model.enums.Shift;
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

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "poll_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PollTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "shift", nullable = false)
    private Shift shift;

    @Column(name = "default_start_time")
    private LocalTime defaultStartTime;

    @Column(name = "default_end_time")
    private LocalTime defaultEndTime;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "template_universities",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "university_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<University> targetUniversities = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "template_buses",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "bus_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Bus> defaultBuses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "template_options",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PollOption> defaultOptions = new HashSet<>();

    @OneToMany(mappedBy = "template")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Poll> polls = new HashSet<>();
}
