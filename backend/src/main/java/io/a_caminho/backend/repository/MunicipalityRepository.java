package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {

    boolean existsByMunicipalityName(String municipalityName);

    Optional<Municipality> findByMunicipalityName(String municipalityName);

    Optional<Municipality> findByUser_UserId(UUID userId);

    @Query("SELECT m FROM Municipality m JOIN m.city c JOIN c.state s WHERE s.stateName = :stateName")
    List<Municipality> findAllMunicipalityFromStateByStateName(@Param("stateName") String stateName);
}
