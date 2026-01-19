package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    List<University> findAllUniversityByNameContainingIgnoreCase(String name);
}