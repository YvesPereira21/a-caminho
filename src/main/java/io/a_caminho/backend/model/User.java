package io.a_caminho.backend.model;

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

import java.util.UUID;

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
    private UUID userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private UserRole role;

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
}
