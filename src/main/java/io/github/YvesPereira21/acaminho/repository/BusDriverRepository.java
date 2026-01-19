package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.BusDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusDriverRepository extends JpaRepository<BusDriver, UUID> {
    Optional<BusDriver> findBusDriverByBusDriverId(UUID id);
    List<BusDriver> findAllByBusDriverNameContainingIgnoreCase(String name);
}