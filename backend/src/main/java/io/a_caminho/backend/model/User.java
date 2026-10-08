package io.a_caminho.backend.model;

import java.util.UUID;

import io.a_caminho.backend.model.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private UUID userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "google_id")
    private String googleId;

    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Municipality municipality;

    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BusDriver busDriver;

    @OneToOne(mappedBy = "user")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UniversityStudent universityStudent;

    public UUID getId() {
        return userId;
    }

    public String getName() {
        if (universityStudent != null && universityStudent.getStudentName() != null) {
            return universityStudent.getStudentName();
        }
        if (municipality != null && municipality.getMunicipalityName() != null) {
            return municipality.getMunicipalityName();
        }
        if (busDriver != null && busDriver.getBusDriverName() != null) {
            return busDriver.getBusDriverName();
        }
        return email;
    }
}
