package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UniversityStudentRepository extends JpaRepository<UniversityStudent, UUID> {
}