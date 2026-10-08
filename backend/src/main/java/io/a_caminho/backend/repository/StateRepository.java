package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {

    boolean existsByStateName(String stateName);

    Optional<State> findByStateName(String stateName);
}
