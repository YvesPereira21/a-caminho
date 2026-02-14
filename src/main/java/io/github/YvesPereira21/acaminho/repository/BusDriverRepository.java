package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.BusDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusDriverRepository extends JpaRepository<BusDriver, UUID> {
    Optional<BusDriver> findByBusDriverId(UUID id);
    List<BusDriver> findAllByMunicipality_User_UserId(UUID userId);
    Optional<BusDriver> findByBusDriverIdAndMunicipality_User_UserId(UUID id, UUID userId);
}