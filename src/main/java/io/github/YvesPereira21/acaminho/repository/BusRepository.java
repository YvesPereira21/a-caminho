package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {
    Optional<Bus> findByBusIdAndMunicipality_User_UserId(UUID busId, UUID municipalityUserId);
    List<Bus> findAllByMunicipality_User_UserId(UUID municipalityUserId);
}