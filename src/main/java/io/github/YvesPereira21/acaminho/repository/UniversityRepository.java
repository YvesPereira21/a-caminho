package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    List<University> findAllUniversityByNameContainingIgnoreCase(String name);
    @Query("SELECT u FROM University u JOIN City c ON u.city = c.cityId " +
            "JOIN State s ON c.state = s.stateId WHERE s.stateName = :stateName")
    List<University> findAllUniversityFromStateByStateName(@Param("stateName") String stateName);
}