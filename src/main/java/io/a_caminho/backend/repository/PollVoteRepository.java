package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.PollVote;
import io.a_caminho.backend.repository.projection.OptionVoteCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {
    boolean existsByPoll_PollIdAndStudent_User_UserId(UUID pollId, UUID studentUserId);

    boolean existsByOption_OptionId(UUID optionId);

    @Query("SELECT pv.option.optionId AS optionId, COUNT(pv) AS total " +
            "FROM PollVote pv WHERE pv.poll.pollId = :pollId" +
            " GROUP BY pv.option.optionId")
    List<OptionVoteCount> countVotesByOption(UUID pollId);

    Optional<PollVote> findByPoll_PollIdAndStudent_User_UserId(UUID pollId, UUID studentUserId);

    void deleteByPoll_PollIdAndStudent_User_UserId(UUID pollId, UUID studentUserId);

    @Query("SELECT pv FROM PollVote pv " +
            "JOIN FETCH pv.student s " +
            "JOIN FETCH pv.option o " +
            "WHERE pv.poll.pollId = :pollId " +
            "ORDER BY o.stopName ASC, s.studentName ASC")
    List<PollVote> findAllWithStudentAndOptionByPollId(@Param("pollId") UUID pollId);
}
