package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Travel;
import io.a_caminho.backend.model.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Travel> findByMunicipality_MunicipalityIdAndStatus(UUID municipalityId, TravelStatus status);

    @Query("SELECT DISTINCT t FROM Travel t " +
            "LEFT JOIN t.targetUniversities u " +
            "WHERE t.municipality.municipalityId = :municipalityId " +
            "AND t.status = :status " +
            "AND (:universityId IS NULL OR u.universityId = :universityId OR SIZE(t.targetUniversities) = 0)")
    List<Travel> findActiveTravelsForStudent(
            @Param("municipalityId") UUID municipalityId,
            @Param("universityId") UUID universityId,
            @Param("status") TravelStatus status
    );
}
