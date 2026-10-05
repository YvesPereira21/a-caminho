package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.PollTemplate;
import io.a_caminho.backend.model.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PollTemplateRepository extends JpaRepository<PollTemplate, UUID> {
    List<PollTemplate> findAllByMunicipality_User_UserId(UUID municipalityUserId);
    List<PollTemplate> findAllByActiveTrue();
    boolean existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
            UUID municipalityMunicipalityId, String routeName, Shift shift
    );
}
