package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {
    Optional<Municipality> findByMunicipalityName(String municipalityName);
    Optional<Municipality> findByUser_UserId(UUID userId);
    @Query("SELECT m FROM Municipality m JOIN City c ON m.city = c.cityId " +
            "JOIN State s ON c.state = s.stateId WHERE s.stateName = :stateName")
    List<Municipality> findAllMunicipalityFromStateByStateName(@Param("stateName") String stateName);
}