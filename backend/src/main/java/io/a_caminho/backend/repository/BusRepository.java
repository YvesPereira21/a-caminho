package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusRepository extends JpaRepository<Bus, UUID> {

    boolean existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(UUID municipalityId, String busName);

    List<Bus> findAllByMunicipality_User_UserId(UUID municipalityUserId);

    List<Bus> findAllByMunicipality_MunicipalityId(UUID municipalityId);

    Optional<Bus> findByBusIdAndMunicipality_User_UserId(UUID busId, UUID municipalityUserId);
}
