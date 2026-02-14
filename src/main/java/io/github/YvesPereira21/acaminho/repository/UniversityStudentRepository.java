package io.github.YvesPereira21.acaminho.repository;

import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UniversityStudentRepository extends JpaRepository<UniversityStudent, UUID> {
    List<UniversityStudent> findAllByMunicipality_User_UserId(UUID municipalityUserUserId);
    List<UniversityStudent> findAllByUniversity_UniversityId(UUID universityId);
}