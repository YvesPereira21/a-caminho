package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByCityNameAndState_StateName(String cityName, String stateName);
    List<City> findAllByState_StateName(String stateName);
}