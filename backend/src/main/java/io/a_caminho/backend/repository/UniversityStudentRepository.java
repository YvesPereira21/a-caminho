package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.UniversityStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityStudentRepository extends JpaRepository<UniversityStudent, UUID> {
    Optional<UniversityStudent> findByCpf(String cpf);
    Optional<UniversityStudent> findByUser_UserId(UUID userId);
    boolean existsByCpf(String cpf);
    boolean existsByRegistrationNumber(String registrationNumber);
}
