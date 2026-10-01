package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.BusDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusDriverRepository extends JpaRepository<BusDriver, UUID> {

    Optional<BusDriver> findByBusDriverId(UUID id);

    List<BusDriver> findAllByMunicipality_User_UserId(UUID userId);

    Optional<BusDriver> findByBusDriverIdAndMunicipality_User_UserId(
            UUID busDriverId,
            UUID municipalityUserId
    );
}
