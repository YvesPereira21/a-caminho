package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
}