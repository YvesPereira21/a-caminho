package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Travel;
import io.a_caminho.backend.model.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelRepository extends JpaRepository<Travel, UUID> {

    boolean existsByBusDriver_BusDriverIdAndStatus(UUID busDriverId, TravelStatus status);

    boolean existsByBus_BusIdAndStatus(UUID busId, TravelStatus status);

    boolean existsByBus_BusId(UUID busId);

    Optional<Travel> findByBusDriver_User_UserIdAndStatus(UUID userId, TravelStatus status);

    List<Travel> findAllByBusDriver_User_UserIdOrderByTravelDateDescDepartureTimeDesc(UUID busDriverUserId);

    List<Travel> findAllByMunicipality_User_UserIdOrderByTravelDateDescDepartureTimeDesc(UUID municipalityUserId);
}
