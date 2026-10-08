package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PollOptionRepository extends JpaRepository<PollOption, UUID> {
    List<PollOption> findAllByMunicipality_User_UserId(UUID municipalityUserId);

    boolean existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(UUID municipalityId, String stopName);
}
