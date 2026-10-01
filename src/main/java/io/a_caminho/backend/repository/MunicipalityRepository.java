package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {
}
