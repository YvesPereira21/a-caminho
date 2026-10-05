package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.pollvote.VoteRequestDTO;
import io.a_caminho.backend.dto.pollvote.VoteResponseDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollVote;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.UniversityStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PollVoteService")
class PollVoteServiceTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollOptionRepository pollOptionRepository;

    @Mock
    private UniversityStudentRepository universityStudentRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @InjectMocks
    private PollVoteService pollVoteService;

    private UUID studentUserId;
    private UUID municipalityUserId;
    private UUID otherUserId;
    private UUID pollId;
    private UUID optionId;
    private UUID otherOptionId;
    private UUID universityId;
    private UUID otherUniversityId;

    private University university;
    private University otherUniversity;
    private UniversityStudent student;
    private Municipality municipality;
    private PollOption pollOption;
    private PollOption otherOption;
    private Poll poll;
    private PollVote pollVote;

    @BeforeEach
    void setUp() {
        studentUserId = UUID.randomUUID();
        municipalityUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        pollId = UUID.randomUUID();
        optionId = UUID.randomUUID();
        otherOptionId = UUID.randomUUID();
        universityId = UUID.randomUUID();
        otherUniversityId = UUID.randomUUID();

        User studentUser = User.builder()
                .userId(studentUserId)
                .email("aluno@ufpb.br")
                .role(UserRole.STUDENT)
                .build();

        User municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(UUID.randomUUID())
                .municipalityName("Prefeitura Municipal")
                .user(municipalityUser)
                .build();

        university = University.builder()
                .universityId(universityId)
                .name("Universidade Federal da Paraíba")
                .build();

        otherUniversity = University.builder()
                .universityId(otherUniversityId)
                .name("Universidade Estadual da Paraíba")
                .build();

        student = UniversityStudent.builder()
                .universityStudentId(UUID.randomUUID())
                .studentName("Ana Silva")
                .registrationNumber("202300123")
                .courseName("Engenharia de Software")
                .user(studentUser)
                .municipality(municipality)
                .university(university)
                .build();

        pollOption = PollOption.builder()
                .optionId(optionId)
                .stopName("Praça Central")
                .municipality(municipality)
                .build();

        otherOption = PollOption.builder()
                .optionId(otherOptionId)
                .stopName("Ponto Não Vinculado")
                .municipality(municipality)
                .build();

        poll = Poll.builder()
                .pollId(pollId)
                .routeName("Rota Universitária")
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .municipality(municipality)
                .targetUniversities(Set.of(university))
                .options(Set.of(pollOption))
                .build();

        pollVote = PollVote.builder()
                .voteId(UUID.randomUUID())
                .poll(poll)
                .student(student)
                .option(pollOption)
                .returnConfirmed(true)
                .voteTime(LocalDateTime.now().minusMinutes(30))
                .build();
    }

    @Nested
    @DisplayName("Cenários de vote (Votação)")
    class VoteTests {

        @Test
        @DisplayName("Deve registrar voto com sucesso quando dados forem válidos")
        void shouldVoteSuccessfullyWhenDataIsValid() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(false);
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.of(pollOption));

            pollVoteService.vote(studentUserId, pollId, requestDTO);

            ArgumentCaptor<PollVote> voteCaptor = ArgumentCaptor.forClass(PollVote.class);
            verify(pollVoteRepository).save(voteCaptor.capture());
            PollVote savedVote = voteCaptor.getValue();

            assertEquals(poll, savedVote.getPoll());
            assertEquals(student, savedVote.getStudent());
            assertEquals(pollOption, savedVote.getOption());
            assertTrue(savedVote.getReturnConfirmed());
            assertNotNull(savedVote.getVoteTime());
        }

        @Test
        @DisplayName("Deve assumir retorno confirmado como true quando parâmetro for null")
        void shouldDefaultReturnConfirmedToTrueWhenNull() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, null);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(false);
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.of(pollOption));

            pollVoteService.vote(studentUserId, pollId, requestDTO);

            ArgumentCaptor<PollVote> voteCaptor = ArgumentCaptor.forClass(PollVote.class);
            verify(pollVoteRepository).save(voteCaptor.capture());
            assertTrue(voteCaptor.getValue().getReturnConfirmed());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estudante não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenStudentNotFound() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Usuário não encontrado.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando enquete não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenPollNotFound() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Enquete não encontrada.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando enquete estiver encerrada")
        void shouldThrowCanNotVoteExceptionWhenPollIsClosed() {
            poll.setEndTime(LocalDateTime.now().minusMinutes(5));
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("A enquete não está mais aberta para votar.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando enquete ainda não iniciou a votação")
        void shouldThrowCanNotVoteExceptionWhenPollNotYetOpen() {
            poll.setStartTime(LocalDateTime.now().plusHours(1));
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("A enquete não está mais aberta para votar.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando universidade do estudante não for atendida")
        void shouldThrowCanNotVoteExceptionWhenStudentUniversityNotTargeted() {
            student.setUniversity(otherUniversity);
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Você não faz parte dessa universidade para poder votar.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando estudante já tiver votado na enquete")
        void shouldThrowCanNotVoteExceptionWhenStudentAlreadyVoted() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(true);

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Você já votou na enquete.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ponto de parada não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenOptionNotFound() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(false);
            when(pollOptionRepository.findById(optionId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Ponto não foi encontrado.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ponto de parada não pertencer à rota da enquete")
        void shouldThrowObjectNotFoundExceptionWhenOptionDoesNotBelongToPoll() {
            VoteRequestDTO requestDTO = new VoteRequestDTO(otherOptionId, true);

            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(false);
            when(pollOptionRepository.findById(otherOptionId)).thenReturn(Optional.of(otherOption));

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.vote(studentUserId, pollId, requestDTO)
            );

            assertEquals("Esse ponto não pertence a enquete.", exception.getMessage());
            verify(pollVoteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getMyVote (Consulta de Voto Próprio)")
    class GetMyVoteTests {

        @Test
        @DisplayName("Deve retornar dados do voto registrado pelo estudante com sucesso")
        void shouldReturnMyVoteWhenVoteExists() {
            when(pollVoteRepository.findByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId))
                    .thenReturn(Optional.of(pollVote));

            VoteResponseDTO result = pollVoteService.getMyVote(studentUserId, pollId);

            assertNotNull(result);
            assertEquals(pollVote.getVoteId(), result.voteId());
            assertEquals(pollId, result.pollId());
            assertEquals(optionId, result.optionId());
            assertEquals("Praça Central", result.stopName());
            assertTrue(result.returnConfirmed());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estudante ainda não tiver votado")
        void shouldThrowObjectNotFoundExceptionWhenVoteNotFound() {
            when(pollVoteRepository.findByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.getMyVote(studentUserId, pollId)
            );

            assertEquals("Você ainda não votou nesta enquete.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de cancelVote (Cancelamento de Voto)")
    class CancelVoteTests {

        @Test
        @DisplayName("Deve cancelar voto com sucesso quando enquete estiver aberta e voto existir")
        void shouldCancelVoteSuccessfullyWhenPollIsOpenAndVoteExists() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(true);

            pollVoteService.cancelVote(studentUserId, pollId);

            verify(pollVoteRepository).deleteByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando enquete não for encontrada ao cancelar voto")
        void shouldThrowObjectNotFoundExceptionWhenPollNotFoundOnCancel() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.cancelVote(studentUserId, pollId)
            );

            assertEquals("Enquete não encontrada.", exception.getMessage());
            verify(pollVoteRepository, never()).deleteByPoll_PollIdAndStudent_User_UserId(any(), any());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando enquete já estiver encerrada ao tentar cancelar voto")
        void shouldThrowCanNotVoteExceptionWhenPollIsClosedOnCancel() {
            poll.setEndTime(LocalDateTime.now().minusHours(1));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollVoteService.cancelVote(studentUserId, pollId)
            );

            assertEquals("Não é mais possível cancelar o voto após o encerramento da enquete.", exception.getMessage());
            verify(pollVoteRepository, never()).deleteByPoll_PollIdAndStudent_User_UserId(any(), any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando voto não for encontrado para cancelamento")
        void shouldThrowObjectNotFoundExceptionWhenVoteNotFoundOnCancel() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.existsByPoll_PollIdAndStudent_User_UserId(pollId, studentUserId)).thenReturn(false);

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.cancelVote(studentUserId, pollId)
            );

            assertEquals("Voto não encontrado para cancelamento.", exception.getMessage());
            verify(pollVoteRepository, never()).deleteByPoll_PollIdAndStudent_User_UserId(any(), any());
        }
    }

    @Nested
    @DisplayName("Cenários de getPollPassengers (Lista Nominal de Passageiros)")
    class GetPollPassengersTests {

        @Test
        @DisplayName("Deve retornar lista nominal de passageiros quando prefeitura for a proprietária")
        void shouldReturnPassengersListWhenMunicipalityIsOwner() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(pollVoteRepository.findAllWithStudentAndOptionByPollId(pollId)).thenReturn(List.of(pollVote));

            List<PollPassengerDTO> passengers = pollVoteService.getPollPassengers(municipalityUserId, pollId);

            assertNotNull(passengers);
            assertEquals(1, passengers.size());
            PollPassengerDTO passenger = passengers.get(0);
            assertEquals("Ana Silva", passenger.studentName());
            assertEquals("202300123", passenger.registrationNumber());
            assertEquals("Engenharia de Software", passenger.courseName());
            assertEquals("Praça Central", passenger.stopName());
            assertTrue(passenger.returnConfirmed());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando enquete não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenPollNotFoundOnGetPassengers() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollVoteService.getPollPassengers(municipalityUserId, pollId)
            );

            assertEquals("Enquete não encontrada.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando prefeitura não for a dona da enquete")
        void shouldThrowUserIsNotOwnerExceptionWhenMunicipalityIsNotOwnerOnGetPassengers() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollVoteService.getPollPassengers(otherUserId, pollId)
            );

            assertEquals("Você não tem permissão para isso.", exception.getMessage());
            verify(pollVoteRepository, never()).findAllWithStudentAndOptionByPollId(any());
        }
    }
}
