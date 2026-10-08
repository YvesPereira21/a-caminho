package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollTemplate;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollTemplateRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.UniversityStudentRepository;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.repository.projection.OptionVoteCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PollService")
class PollServiceTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollTemplateRepository pollTemplateRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private UniversityStudentRepository universityStudentRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PollMapper pollMapper;

    @InjectMocks
    private PollService pollService;

    private UUID municipalityUserId;
    private UUID otherMunicipalityUserId;
    private UUID studentUserId;
    private UUID municipalityId;
    private UUID otherMunicipalityId;
    private UUID universityId;
    private UUID otherUniversityId;
    private UUID pollId;
    private UUID optionId;

    private User municipalityUser;
    private User studentUser;
    private User otherUser;
    private Municipality municipality;
    private Municipality otherMunicipality;
    private University university;
    private University otherUniversity;
    private UniversityStudent student;
    private PollOption pollOption;
    private Poll poll;
    private PollTemplate pollTemplate;
    private PollListDTO pollListDTO;

    @BeforeEach
    void setUp() {
        municipalityUserId = UUID.randomUUID();
        otherMunicipalityUserId = UUID.randomUUID();
        studentUserId = UUID.randomUUID();
        municipalityId = UUID.randomUUID();
        otherMunicipalityId = UUID.randomUUID();
        universityId = UUID.randomUUID();
        otherUniversityId = UUID.randomUUID();
        pollId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        studentUser = User.builder()
                .userId(studentUserId)
                .email("aluno@ufpb.br")
                .role(UserRole.STUDENT)
                .build();

        otherUser = User.builder()
                .userId(otherMunicipalityUserId)
                .email("outro@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build();

        State state = State.builder()
                .stateId(UUID.randomUUID())
                .stateName("Paraíba")
                .build();

        City city = City.builder()
                .cityId(UUID.randomUUID())
                .cityName("João Pessoa")
                .state(state)
                .build();

        municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .municipalityName("Prefeitura de João Pessoa")
                .user(municipalityUser)
                .city(city)
                .build();

        otherMunicipality = Municipality.builder()
                .municipalityId(otherMunicipalityId)
                .municipalityName("Prefeitura de Campina Grande")
                .user(otherUser)
                .city(city)
                .build();

        university = University.builder()
                .universityId(universityId)
                .name("Universidade Federal da Paraíba")
                .campus("Campus I")
                .city(city)
                .build();

        otherUniversity = University.builder()
                .universityId(otherUniversityId)
                .name("Universidade Estadual da Paraíba")
                .campus("Campus Central")
                .city(city)
                .build();

        student = UniversityStudent.builder()
                .universityStudentId(UUID.randomUUID())
                .studentName("João Estudante")
                .user(studentUser)
                .municipality(municipality)
                .university(university)
                .build();

        pollOption = PollOption.builder()
                .optionId(optionId)
                .stopName("Praça Central")
                .municipality(municipality)
                .build();

        poll = Poll.builder()
                .pollId(pollId)
                .routeName("Rota Universitária Manhã")
                .shift(Shift.MORNING)
                .pollDate(LocalDate.now())
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(5))
                .totalCapacity(45)
                .confirmedVotesCount(10)
                .municipality(municipality)
                .targetUniversities(Set.of(university))
                .options(Set.of(pollOption))
                .build();

        pollTemplate = PollTemplate.builder()
                .templateId(UUID.randomUUID())
                .routeName("Rota Universitária Manhã")
                .shift(Shift.MORNING)
                .defaultStartTime(LocalTime.of(0, 0))
                .defaultEndTime(LocalTime.of(23, 59))
                .active(true)
                .municipality(municipality)
                .targetUniversities(Set.of(university))
                .defaultOptions(Set.of(pollOption))
                .build();

        pollListDTO = new PollListDTO(
                pollId,
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalDate.now(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(5),
                10
        );
    }

    @Nested
    @DisplayName("Cenários de createPoll (Agendamento)")
    class CreatePollTests {

        @Test
        @DisplayName("Deve gerar enquetes diárias para templates ativos elegíveis")
        void shouldCreatePollsFromActiveTemplates() {
            when(pollTemplateRepository.findAllByActiveTrue())
                    .thenReturn(List.of(pollTemplate));
            when(pollRepository.existsByMunicipality_MunicipalityIdAndShiftAndPollDate(
                    eq(municipalityId), eq(Shift.MORNING), any(LocalDate.class)))
                    .thenReturn(false);

            pollService.createPoll();

            verify(pollRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Não deve gerar enquetes quando template já gerou enquete para o dia e turno")
        void shouldNotCreatePollWhenPollAlreadyExistsForToday() {
            when(pollTemplateRepository.findAllByActiveTrue())
                    .thenReturn(List.of(pollTemplate));
            when(pollRepository.existsByMunicipality_MunicipalityIdAndShiftAndPollDate(
                    eq(municipalityId), eq(Shift.MORNING), any(LocalDate.class)))
                    .thenReturn(true);

            pollService.createPoll();

            verify(pollRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Não deve chamar saveAll quando não houver templates ativos")
        void shouldNotCallSaveAllWhenNoTemplatesActive() {
            when(pollTemplateRepository.findAllByActiveTrue())
                    .thenReturn(Collections.emptyList());

            pollService.createPoll();

            verify(pollRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Cenários de getPoll (Consulta Unificada)")
    class GetPollTests {

        @Test
        @DisplayName("Deve retornar PollDTO com sucesso quando usuário for prefeitura proprietária")
        void shouldReturnPollDTOWhenMunicipalityOwner() {
            OptionVoteCount voteCount = mock(OptionVoteCount.class);
            when(voteCount.getOptionId()).thenReturn(optionId);
            when(voteCount.getTotal()).thenReturn(5L);

            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(municipalityUserId)).thenReturn(Optional.of(municipalityUser));
            when(pollVoteRepository.countVotesByOption(pollId)).thenReturn(List.of(voteCount));

            PollDTO result = pollService.getPoll(municipalityUserId, pollId);

            assertNotNull(result);
            assertEquals(pollId, result.pollId());
            assertEquals("Rota Universitária Manhã", result.routeName());
            assertEquals(1, result.options().size());
            assertEquals(1, result.targetUniversities().size());
        }

        @Test
        @DisplayName("Deve retornar PollDTO com sucesso quando usuário for estudante elegível da rota")
        void shouldReturnPollDTOWhenStudentIsEligible() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(studentUserId)).thenReturn(Optional.of(studentUser));
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollVoteRepository.countVotesByOption(pollId)).thenReturn(Collections.emptyList());

            PollDTO result = pollService.getPoll(studentUserId, pollId);

            assertNotNull(result);
            assertEquals(pollId, result.pollId());
            assertEquals("Rota Universitária Manhã", result.routeName());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando enquete não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenPollNotFound() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollService.getPoll(municipalityUserId, pollId)
            );

            assertEquals("Enquete não encontrada.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando usuário não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenUserNotFound() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(municipalityUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollService.getPoll(municipalityUserId, pollId)
            );

            assertEquals("Usuário não encontrado.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando prefeitura não for a dona da enquete")
        void shouldThrowUserIsNotOwnerExceptionWhenMunicipalityIsNotOwner() {
            UUID otherMuniUserId = UUID.randomUUID();
            User otherMuniUser = User.builder()
                    .userId(otherMuniUserId)
                    .role(UserRole.MUNICIPALITY)
                    .build();

            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(otherMuniUserId)).thenReturn(Optional.of(otherMuniUser));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollService.getPoll(otherMuniUserId, pollId)
            );

            assertEquals("Você não tem permissão para visualizar esta enquete pois não é o proprietário.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando perfil do estudante não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenStudentProfileNotFound() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(studentUserId)).thenReturn(Optional.of(studentUser));
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollService.getPoll(studentUserId, pollId)
            );

            assertEquals("Estudante não encontrado.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException quando estudante for de outro município")
        void shouldThrowCrossMunicipalityAccessExceptionWhenStudentFromOtherMunicipality() {
            UniversityStudent otherCityStudent = UniversityStudent.builder()
                    .universityStudentId(UUID.randomUUID())
                    .user(studentUser)
                    .municipality(otherMunicipality)
                    .university(university)
                    .build();

            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(studentUserId)).thenReturn(Optional.of(studentUser));
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(otherCityStudent));

            CrossMunicipalityAccessException exception = assertThrows(
                    CrossMunicipalityAccessException.class,
                    () -> pollService.getPoll(studentUserId, pollId)
            );

            assertEquals("O estudante não pertence ao município responsável por esta enquete.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar CanNotVoteException quando estudante for de universidade não atendida pela rota")
        void shouldThrowCanNotVoteExceptionWhenStudentUniversityNotTargeted() {
            UniversityStudent otherUnivStudent = UniversityStudent.builder()
                    .universityStudentId(UUID.randomUUID())
                    .user(studentUser)
                    .municipality(municipality)
                    .university(otherUniversity)
                    .build();

            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(studentUserId)).thenReturn(Optional.of(studentUser));
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(otherUnivStudent));

            CanNotVoteException exception = assertThrows(
                    CanNotVoteException.class,
                    () -> pollService.getPoll(studentUserId, pollId)
            );

            assertEquals("O estudante não pertence à universidade atendida por esta enquete.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário possuir outra role não autorizada")
        void shouldThrowUserIsNotOwnerExceptionWhenUserHasOtherRole() {
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(userRepository.findById(otherMunicipalityUserId)).thenReturn(Optional.of(otherUser));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> pollService.getPoll(otherMunicipalityUserId, pollId)
            );

            assertEquals("Você não tem permissão para visualizar esta enquete.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllPollFromMunicipality")
    class GetAllPollFromMunicipalityTests {

        @Test
        @DisplayName("Deve retornar lista de enquetes da prefeitura com sucesso")
        void shouldReturnListOfPollsWhenMunicipalityFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.of(municipality));
            when(pollRepository.findAllByMunicipality_User_UserId(municipalityUserId)).thenReturn(List.of(poll));
            when(pollMapper.toDTOList(poll)).thenReturn(pollListDTO);

            List<PollListDTO> result = pollService.getAllPollFromMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Rota Universitária Manhã", result.get(0).routeName());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollService.getAllPollFromMunicipality(municipalityUserId)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getOpenPollsToStudent")
    class GetOpenPollsToStudentTests {

        @Test
        @DisplayName("Deve retornar lista de enquetes abertas para o estudante com sucesso")
        void shouldReturnOpenPollsForStudent() {
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.of(student));
            when(pollRepository.findOpenPollsForStudent(eq(municipalityId), eq(universityId), any(LocalDateTime.class)))
                    .thenReturn(List.of(poll));
            when(pollMapper.toDTOList(poll)).thenReturn(pollListDTO);

            List<PollListDTO> result = pollService.getOpenPollsToStudent(studentUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Rota Universitária Manhã", result.get(0).routeName());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estudante não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenStudentNotFound() {
            when(universityStudentRepository.findByUser_UserId(studentUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> pollService.getOpenPollsToStudent(studentUserId)
            );

            assertEquals("Usuário não encontrado.", exception.getMessage());
        }
    }
}
