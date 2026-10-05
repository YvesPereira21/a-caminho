package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollMapper;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollTemplate;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollTemplateRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.UniversityStudentRepository;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.repository.projection.OptionVoteCount;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PollService {

    private final PollRepository pollRepository;
    private final PollTemplateRepository pollTemplateRepository;
    private final MunicipalityRepository municipalityRepository;
    private final UniversityStudentRepository universityStudentRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;
    private final PollMapper pollMapper;

    public PollService(
            PollRepository pollRepository,
            PollTemplateRepository pollTemplateRepository,
            MunicipalityRepository municipalityRepository,
            UniversityStudentRepository universityStudentRepository,
            PollVoteRepository pollVoteRepository,
            UserRepository userRepository,
            PollMapper pollMapper
    ) {
        this.pollRepository = pollRepository;
        this.pollTemplateRepository = pollTemplateRepository;
        this.municipalityRepository = municipalityRepository;
        this.universityStudentRepository = universityStudentRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.userRepository = userRepository;
        this.pollMapper = pollMapper;
    }

    @Scheduled(cron = "0 0/15 * * * MON-FRI", zone = "America/Sao_Paulo")
    public void createPoll(){
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        List<Poll> pollsToSave = pollTemplateRepository
                .findAllByActiveTrue()
                .stream()
                .filter(template -> !now.isBefore(template.getDefaultStartTime()))
                .filter(template -> !pollRepository.existsByMunicipality_MunicipalityIdAndShiftAndPollDate(
                        template.getMunicipality().getMunicipalityId(),
                        template.getShift(),
                        today
                ))
                .map(template -> buildPollFromPollTemplate(template, today))
                .toList();

        if (!pollsToSave.isEmpty()) {
            pollRepository.saveAll(pollsToSave);
        }
    }

    @Transactional(readOnly = true)
    public PollDTO getPoll(UUID userId, UUID pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ObjectNotFoundException("Enquete não encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado."));

        if (user.getRole() == UserRole.MUNICIPALITY) {
            validateMunicipalityAccess(poll, userId);
        } else if (user.getRole() == UserRole.STUDENT) {
            validateStudentAccess(poll, userId);
        } else {
            throw new UserIsNotOwnerException("Você não tem permissão para visualizar esta enquete.");
        }

        return mapToPollDTO(poll);
    }

    @Transactional(readOnly = true)
    public List<PollListDTO> getAllPollFromMunicipality(UUID municipalityUserId){
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        return pollRepository.findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(pollMapper::toDTOList)
                .toList();
    }

    public List<PollListDTO> getOpenPollsToStudent(UUID studentUserId){
        LocalDateTime now = LocalDateTime.now();
        UniversityStudent student = universityStudentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado."));

        return pollRepository
                .findOpenPollsForStudent(
                student.getMunicipality().getMunicipalityId(), student.getUniversity().getUniversityId(), now
        )
                .stream()
                .map(pollMapper::toDTOList)
                .toList();
    }

    private Poll buildPollFromPollTemplate(PollTemplate pollTemplate, LocalDate today){
        LocalDateTime startTime = today.atTime(pollTemplate.getDefaultStartTime());

        LocalDateTime endTime = pollTemplate.getDefaultEndTime().isBefore(pollTemplate.getDefaultStartTime())
                ? today.plusDays(1).atTime(pollTemplate.getDefaultEndTime())
                : today.atTime(pollTemplate.getDefaultEndTime());

        return Poll.builder()
                .routeName(pollTemplate.getRouteName())
                .municipality(pollTemplate.getMunicipality())
                .pollDate(today)
                .shift(pollTemplate.getShift())
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(LocalDateTime.now())
                .options(new HashSet<>(pollTemplate.getDefaultOptions()))
                .targetUniversities(new HashSet<>(pollTemplate.getTargetUniversities()))
                .build();
    }

    private PollDTO mapToPollDTO(Poll poll) {
        Map<UUID, Long> countTotalVotesByOption = pollVoteRepository.countVotesByOption(poll.getPollId())
                .stream()
                .collect(Collectors.toMap(OptionVoteCount::getOptionId, OptionVoteCount::getTotal));

        Set<PollOptionResponseDTO> options = poll.getOptions()
                .stream()
                .map(option -> new PollOptionResponseDTO(
                        option.getOptionId(),
                        option.getStopName(),
                        countTotalVotesByOption.getOrDefault(option.getOptionId(), 0L)
                ))
                .collect(Collectors.toSet());

        Set<UniversityResponseDTO> targetUniversities = poll.getTargetUniversities()
                .stream()
                .map(university -> new UniversityResponseDTO(
                        university.getUniversityId(),
                        university.getName(),
                        university.getCampus(),
                        university.getCity().getCityName(),
                        university.getCity().getState().getStateName()
                ))
                .collect(Collectors.toSet());

        return new PollDTO(
                poll.getPollId(),
                poll.getRouteName(),
                poll.getShift(),
                poll.getPollDate(),
                poll.getStartTime(),
                poll.getEndTime(),
                poll.getTotalCapacity(),
                poll.getConfirmedVotesCount(),
                targetUniversities,
                options
        );
    }

    private void validateMunicipalityAccess(Poll poll, UUID userId) {
        if (!poll.getMunicipality().getUser().getId().equals(userId)) {
            throw new UserIsNotOwnerException("Você não tem permissão para visualizar esta enquete pois não é o proprietário.");
        }
    }

    private void validateStudentAccess(Poll poll, UUID userId) {
        UniversityStudent student = universityStudentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Estudante não encontrado."));

        boolean sameMunicipality = poll.getMunicipality().getMunicipalityId()
                .equals(student.getMunicipality().getMunicipalityId());
        if (!sameMunicipality) {
            throw new CrossMunicipalityAccessException("O estudante não pertence ao município responsável por esta enquete.");
        }

        boolean studentUniversityEligible = poll.getTargetUniversities().stream()
                .anyMatch(u -> u.getUniversityId().equals(student.getUniversity().getUniversityId()));
        if (!studentUniversityEligible) {
            throw new CanNotVoteException("O estudante não pertence à universidade atendida por esta enquete.");
        }
    }
}
