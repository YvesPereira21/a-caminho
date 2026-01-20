package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {
    Municipality findByMunicipalityName(String municipalityName);
}