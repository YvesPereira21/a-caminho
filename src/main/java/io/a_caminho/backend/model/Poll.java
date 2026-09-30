package io.a_caminho.backend.model;

import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.Shift;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "polls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "poll_id")
    private UUID pollId;

    @Column(name = "poll_date")
    private LocalDate pollDate;

    @Column(name = "shift", nullable = false)
    private Shift shift;

    @Column(name = "direction", nullable = false)
    @Builder.Default
    private PollDirection direction = PollDirection.OUTBOUND;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PollTemplate template;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_poll_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Poll parentPoll;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Formula("(SELECT COALESCE(SUM(b.seats_quantity), 0) FROM poll_buses pb JOIN buses b ON pb.bus_id = b.bus_id WHERE pb.poll_id = poll_id)")
    private Integer totalCapacity;

    @Formula("(SELECT COUNT(*) FROM poll_votes pv WHERE pv.poll_id = poll_id)")
    private Integer confirmedVotesCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "poll_universities",
        joinColumns = @JoinColumn(name = "poll_id"),
        inverseJoinColumns = @JoinColumn(name = "university_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<University> targetUniversities = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "poll_buses",
        joinColumns = @JoinColumn(name = "poll_id"),
        inverseJoinColumns = @JoinColumn(name = "bus_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Bus> assignedBuses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "poll_options_rel",
        joinColumns = @JoinColumn(name = "poll_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PollOption> options = new HashSet<>();

    @OneToMany(mappedBy = "poll")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Travel> travels = new HashSet<>();
}
