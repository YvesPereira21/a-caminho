package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {
    Optional<Bus> findByBusId(UUID id);
    List<Bus> findAllByMunicipality_CityName(String cityName);
}