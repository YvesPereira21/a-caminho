package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PollRepository extends JpaRepository<Poll, UUID> {
    boolean existsByMunicipality_MunicipalityIdAndShiftAndPollDate(
            UUID municipalityId,
            Shift shift,
            LocalDate pollDate
    );

    List<Poll> findAllByMunicipality_User_UserId(UUID municipality_user_userId);

    @Query("SELECT DISTINCT p FROM Poll p JOIN p.targetUniversities u" +
            " WHERE p.municipality.municipalityId = :municipalityId" +
            " AND u.universityId = :universityId" +
            " AND :now BETWEEN p.startTime AND p.endTime")
    List<Poll> findOpenPollsForStudent(
            @Param("municipalityId") UUID municipalityId,
            @Param("universityId") UUID universityId,
            @Param("now") LocalDateTime now
    );

    List<Poll> findByMunicipality_MunicipalityIdAndPollDateOrderByStartTimeAsc(
            UUID municipalityId,
            LocalDate pollDate
    );
}
