package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findByCityNameAndState_StateName(String cityName, String stateName);

    List<City> findAllByState_StateName(String stateName);

    boolean existsByCityNameAndState_StateName(String cityName, String stateName);
}
