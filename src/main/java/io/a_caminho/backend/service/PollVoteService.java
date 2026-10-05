package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.pollvote.VoteRequestDTO;
import io.a_caminho.backend.dto.pollvote.VoteResponseDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollVote;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.UniversityStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PollVoteService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final UniversityStudentRepository universityStudentRepository;
    private final PollVoteRepository pollVoteRepository;

    public PollVoteService(
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            UniversityStudentRepository universityStudentRepository,
            PollVoteRepository pollVoteRepository
    ) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.universityStudentRepository = universityStudentRepository;
        this.pollVoteRepository = pollVoteRepository;
    }

    @Transactional
    public void vote(UUID studentUserId, UUID pollId, VoteRequestDTO vote){
        UniversityStudent student = universityStudentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado."));
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ObjectNotFoundException("Enquete não encontrada."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(poll.getStartTime()) || now.isAfter(poll.getEndTime())) {
            throw new CanNotVoteException("A enquete não está mais aberta para votar.");
        }

        boolean canVote = poll.getTargetUniversities().stream()
                .anyMatch(u -> u.getUniversityId().equals(student.getUniversity().getUniversityId()));
        if(!canVote){
            throw new CanNotVoteException("Você não faz parte dessa universidade para poder votar.");
        }

        boolean studentAlreadyVoted = pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId);
        if(studentAlreadyVoted){
            throw new CanNotVoteException("Você já votou na enquete.");
        }

        PollOption pollOption = pollOptionRepository.findById(vote.optionId())
                .orElseThrow(() -> new ObjectNotFoundException("Ponto não foi encontrado."));

        boolean optionBelongsToPoll = poll.getOptions().stream()
                .anyMatch(o -> o.getOptionId().equals(pollOption.getOptionId()));
        if(!optionBelongsToPoll){
            throw new ObjectNotFoundException("Esse ponto não pertence a enquete.");
        }

        PollVote pollVote = PollVote.builder()
                .poll(poll)
                .student(student)
                .option(pollOption)
                .returnConfirmed(vote.returnConfirmed() != null ? vote.returnConfirmed() : true)
                .voteTime(now)
                .build();

        pollVoteRepository.save(pollVote);
    }

    public VoteResponseDTO getMyVote(UUID studentUserId, UUID pollId) {
        PollVote pollVote = pollVoteRepository.findByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Você ainda não votou nesta enquete."));

        return new VoteResponseDTO(
                pollVote.getVoteId(),
                pollVote.getPoll().getPollId(),
                pollVote.getOption().getOptionId(),
                pollVote.getOption().getStopName(),
                pollVote.getReturnConfirmed(),
                pollVote.getVoteTime()
        );
    }

    @Transactional
    public void cancelVote(UUID studentUserId, UUID pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ObjectNotFoundException("Enquete não encontrada."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(poll.getStartTime()) || now.isAfter(poll.getEndTime())) {
            throw new CanNotVoteException("Não é mais possível cancelar o voto após o encerramento da enquete.");
        }

        boolean studentAlreadyVoted = pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId);
        if (!studentAlreadyVoted) {
            throw new ObjectNotFoundException("Voto não encontrado para cancelamento.");
        }

        pollVoteRepository.deleteByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId);
    }

    public List<PollPassengerDTO> getPollPassengers(UUID municipalityUserId, UUID pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ObjectNotFoundException("Enquete não encontrada."));

        if (!poll.getMunicipality().getUser().getId().equals(municipalityUserId)) {
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        return pollVoteRepository.findAllWithStudentAndOptionByPollId(pollId)
                .stream()
                .map(vote -> new PollPassengerDTO(
                        vote.getVoteId(),
                        vote.getStudent().getUniversityStudentId(),
                        vote.getStudent().getStudentName(),
                        vote.getStudent().getRegistrationNumber(),
                        vote.getStudent().getCourseName(),
                        vote.getOption().getOptionId(),
                        vote.getOption().getStopName(),
                        vote.getReturnConfirmed(),
                        vote.getVoteTime()
                ))
                .toList();
    }
}
