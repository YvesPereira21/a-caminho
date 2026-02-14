package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StateRepository extends JpaRepository<State, UUID> {
    boolean existsByStateName(String stateName);
    Optional<State> findByStateName(String stateName);
}
