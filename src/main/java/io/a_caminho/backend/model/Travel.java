package io.a_caminho.backend.model;

import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.TravelStatus;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "travels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "travel_id")
    private UUID travelId;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "status", nullable = false)
    private TravelStatus status;

    @Column(name = "direction", nullable = false)
    @Builder.Default
    private PollDirection direction = PollDirection.OUTBOUND;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "shift")
    private Shift shift;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "pickup_time")
    private LocalTime pickupTime;

    @Column(name = "return_time")
    private LocalTime returnTime;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_driver_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BusDriver busDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "travel_universities",
        joinColumns = @JoinColumn(name = "travel_id"),
        inverseJoinColumns = @JoinColumn(name = "university_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<University> targetUniversities = new HashSet<>();
}
