package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.TravelMapper;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.Poll;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollVote;
import io.a_caminho.backend.model.Travel;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.TravelStatus;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.BusRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import io.a_caminho.backend.repository.TravelRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TravelService")
class TravelServiceTest {

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private TravelMapper travelMapper;

    @Mock
    private BusDriverRepository busDriverRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @InjectMocks
    private TravelService travelService;

    private UUID busDriverUserId;
    private UUID municipalityUserId;
    private UUID otherUserId;

    private UUID busDriverId;
    private UUID municipalityId;
    private UUID otherMunicipalityId;
    private UUID busId;
    private UUID otherBusId;
    private UUID pollId;
    private UUID travelId;

    private User busDriverUser;
    private User municipalityUser;
    private User otherUser;

    private Municipality municipality;
    private Municipality otherMunicipality;
    private BusDriver busDriver;
    private Bus bus;
    private Bus otherBus;
    private Poll poll;
    private Travel travel;
    private TravelCreateDTO travelCreateDTO;
    private TravelDTO travelDTO;

    @BeforeEach
    void setUp() {
        busDriverUserId = UUID.randomUUID();
        municipalityUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        busDriverId = UUID.randomUUID();
        municipalityId = UUID.randomUUID();
        otherMunicipalityId = UUID.randomUUID();
        busId = UUID.randomUUID();
        otherBusId = UUID.randomUUID();
        pollId = UUID.randomUUID();
        travelId = UUID.randomUUID();

        busDriverUser = User.builder()
                .userId(busDriverUserId)
                .email("motorista@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build();

        municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        otherUser = User.builder()
                .userId(otherUserId)
                .email("outro@municipio.gov.br")
                .role(UserRole.STUDENT)
                .build();

        municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .municipalityName("Guarabira")
                .user(municipalityUser)
                .build();

        otherMunicipality = Municipality.builder()
                .municipalityId(otherMunicipalityId)
                .municipalityName("Campina Grande")
                .user(otherUser)
                .build();

        busDriver = BusDriver.builder()
                .busDriverId(busDriverId)
                .user(busDriverUser)
                .municipality(municipality)
                .busDriverName("Severino Motorista")
                .build();

        bus = Bus.builder()
                .busId(busId)
                .busName("Ônibus 01")
                .seatsQuantity(44)
                .municipality(municipality)
                .build();

        otherBus = Bus.builder()
                .busId(otherBusId)
                .busName("Ônibus 02")
                .seatsQuantity(44)
                .municipality(otherMunicipality)
                .build();

        University university = University.builder()
                .universityId(UUID.randomUUID())
                .name("UFPB")
                .build();

        poll = Poll.builder()
                .pollId(pollId)
                .municipality(municipality)
                .shift(Shift.NIGHT)
                .targetUniversities(new HashSet<>(Set.of(university)))
                .confirmedVotesCount(25)
                .build();

        travel = Travel.builder()
                .travelId(travelId)
                .travelDate(LocalDate.now())
                .status(TravelStatus.IN_PROGRESS)
                .direction(PollDirection.OUTBOUND)
                .departureTime(LocalTime.of(18, 0))
                .returnTime(LocalTime.of(22, 30))
                .shift(Shift.NIGHT)
                .bus(bus)
                .busDriver(busDriver)
                .poll(poll)
                .municipality(municipality)
                .targetUniversities(new HashSet<>(Set.of(university)))
                .build();

        travelCreateDTO = new TravelCreateDTO(pollId, busId, TravelStatus.IN_PROGRESS, LocalTime.of(22, 30));

        BusDriverResponseDTO driverResponseDTO = new BusDriverResponseDTO(
                busDriverId,
                "Severino Motorista",
                "motorista@municipio.gov.br"
        );

        travelDTO = new TravelDTO(
                travelId,
                PollDirection.OUTBOUND,
                driverResponseDTO,
                25,
                Collections.emptySet()
        );
    }

    @Nested
    @DisplayName("Cenários de createTravel (Início da Viagem)")
    class CreateTravelTests {

        @Test
        @DisplayName("Deve iniciar viagem com sucesso para motorista autenticado")
        void shouldCreateTravelSuccessfully() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(travelRepository.existsByBusDriver_BusDriverIdAndStatus(busDriverId, TravelStatus.IN_PROGRESS)).thenReturn(false);
            when(travelRepository.existsByBus_BusIdAndStatus(busId, TravelStatus.IN_PROGRESS)).thenReturn(false);

            Travel unpersistedTravel = new Travel();
            when(travelMapper.toEntity(travelCreateDTO)).thenReturn(unpersistedTravel);
            when(travelRepository.save(any(Travel.class))).thenReturn(travel);
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.createTravel(busDriverUserId, travelCreateDTO);

            assertNotNull(result);
            assertEquals(travelId, result.travelId());
            assertEquals(PollDirection.OUTBOUND, result.direction());

            verify(travelRepository).save(any(Travel.class));
            verify(travelMapper).toDTO(travel);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando motorista não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenBusDriverNotFound() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("Motorista não encontrado.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando município vinculado ao motorista for nulo")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityOnDriverIsNull() {
            busDriver.setMunicipality(null);
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("Município vinculado ao motorista não encontrado.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando enquete não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenPollNotFound() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("Enquete não encontrada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ônibus não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenBusNotFound() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("Ônibus não encontrado.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException quando a enquete pertencer a outro município")
        void shouldThrowCrossMunicipalityAccessExceptionWhenPollBelongsToAnotherMunicipality() {
            poll.setMunicipality(otherMunicipality);
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

            CrossMunicipalityAccessException exception = assertThrows(
                    CrossMunicipalityAccessException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("A enquete selecionada não pertence ao seu município.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException quando o ônibus pertencer a outro município")
        void shouldThrowCrossMunicipalityAccessExceptionWhenBusBelongsToAnotherMunicipality() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.of(otherBus));

            CrossMunicipalityAccessException exception = assertThrows(
                    CrossMunicipalityAccessException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("O ônibus selecionado não pertence ao seu município.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o motorista já possui viagem em andamento")
        void shouldThrowIllegalArgumentExceptionWhenDriverAlreadyHasTravelInProgress() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(travelRepository.existsByBusDriver_BusDriverIdAndStatus(busDriverId, TravelStatus.IN_PROGRESS)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("O motorista já possui uma viagem em andamento.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o ônibus selecionado já está em viagem em andamento")
        void shouldThrowIllegalArgumentExceptionWhenBusAlreadyHasTravelInProgress() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(pollRepository.findById(pollId)).thenReturn(Optional.of(poll));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(travelRepository.existsByBusDriver_BusDriverIdAndStatus(busDriverId, TravelStatus.IN_PROGRESS)).thenReturn(false);
            when(travelRepository.existsByBus_BusIdAndStatus(busId, TravelStatus.IN_PROGRESS)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.createTravel(busDriverUserId, travelCreateDTO)
            );

            assertEquals("O ônibus selecionado já está em uma viagem em andamento.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getTravelById (Busca por ID)")
    class GetTravelByIdTests {

        @Test
        @DisplayName("Deve buscar viagem por ID com sucesso quando o solicitante for o motorista condutor")
        void shouldGetTravelByIdSuccessfullyWhenUserIsDriver() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.getTravelById(travelId, busDriverUserId);

            assertNotNull(result);
            assertEquals(travelId, result.travelId());
            verify(travelRepository).findById(travelId);
        }

        @Test
        @DisplayName("Deve buscar viagem por ID com sucesso quando o solicitante for a prefeitura proprietária")
        void shouldGetTravelByIdSuccessfullyWhenUserIsMunicipality() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.getTravelById(travelId, municipalityUserId);

            assertNotNull(result);
            assertEquals(travelId, result.travelId());
            verify(travelRepository).findById(travelId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenTravelNotFound() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getTravelById(travelId, busDriverUserId)
            );

            assertEquals("Viagem não encontrada.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException quando o usuário não for nem o motorista nem a prefeitura dona")
        void shouldThrowCrossMunicipalityAccessExceptionWhenUserHasNoAccess() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            CrossMunicipalityAccessException exception = assertThrows(
                    CrossMunicipalityAccessException.class,
                    () -> travelService.getTravelById(travelId, otherUserId)
            );

            assertEquals("Você não tem permissão para visualizar os dados desta viagem.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getActiveTravelByDriver (Viagem em Andamento do Motorista)")
    class GetActiveTravelByDriverTests {

        @Test
        @DisplayName("Deve buscar viagem em andamento com sucesso para o motorista")
        void shouldGetActiveTravelByDriverSuccessfully() {
            when(travelRepository.findByBusDriver_User_UserIdAndStatus(busDriverUserId, TravelStatus.IN_PROGRESS))
                    .thenReturn(Optional.of(travel));
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.getActiveTravelByDriver(busDriverUserId);

            assertNotNull(result);
            assertEquals(travelId, result.travelId());
            verify(travelRepository).findByBusDriver_User_UserIdAndStatus(busDriverUserId, TravelStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando não houver viagem em andamento para o motorista")
        void shouldThrowObjectNotFoundExceptionWhenNoActiveTravelFound() {
            when(travelRepository.findByBusDriver_User_UserIdAndStatus(busDriverUserId, TravelStatus.IN_PROGRESS))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getActiveTravelByDriver(busDriverUserId)
            );

            assertEquals("Nenhuma viagem em andamento encontrada para este motorista.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllTravelsByDriver (Histórico do Motorista)")
    class GetAllTravelsByDriverTests {

        @Test
        @DisplayName("Deve listar viagens do motorista com sucesso")
        void shouldGetAllTravelsByDriverSuccessfully() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.of(busDriver));
            when(travelRepository.findAllByBusDriver_User_UserIdOrderByTravelDateDescDepartureTimeDesc(busDriverUserId))
                    .thenReturn(List.of(travel));
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            List<TravelDTO> result = travelService.getAllTravelsByDriver(busDriverUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(travelRepository).findAllByBusDriver_User_UserIdOrderByTravelDateDescDepartureTimeDesc(busDriverUserId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando o motorista não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenDriverNotFound() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getAllTravelsByDriver(busDriverUserId)
            );

            assertEquals("Motorista não encontrado.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllTravelsByMunicipality (Histórico da Prefeitura)")
    class GetAllTravelsByMunicipalityTests {

        @Test
        @DisplayName("Deve listar viagens da prefeitura com sucesso")
        void shouldGetAllTravelsByMunicipalitySuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.of(municipality));
            when(travelRepository.findAllByMunicipality_User_UserIdOrderByTravelDateDescDepartureTimeDesc(municipalityUserId))
                    .thenReturn(List.of(travel));
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            List<TravelDTO> result = travelService.getAllTravelsByMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(travelRepository).findAllByMunicipality_User_UserIdOrderByTravelDateDescDepartureTimeDesc(municipalityUserId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getAllTravelsByMunicipality(municipalityUserId)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getTravelPassengers (Passageiros da Viagem)")
    class GetTravelPassengersTests {

        private PollOption option;
        private UniversityStudent student1;
        private UniversityStudent student2;
        private PollVote voteOutboundAndReturn;
        private PollVote voteOutboundOnly;

        @BeforeEach
        void setUpPassengers() {
            option = PollOption.builder()
                    .optionId(UUID.randomUUID())
                    .stopName("Ponto Central")
                    .municipality(municipality)
                    .build();

            student1 = UniversityStudent.builder()
                    .universityStudentId(UUID.randomUUID())
                    .studentName("Ana Silva")
                    .registrationNumber("2021001")
                    .courseName("Engenharia de Produção")
                    .build();

            student2 = UniversityStudent.builder()
                    .universityStudentId(UUID.randomUUID())
                    .studentName("Bruno Santos")
                    .registrationNumber("2021002")
                    .courseName("Direito")
                    .build();

            voteOutboundAndReturn = PollVote.builder()
                    .voteId(UUID.randomUUID())
                    .poll(poll)
                    .student(student1)
                    .option(option)
                    .returnConfirmed(true)
                    .voteTime(LocalDateTime.now())
                    .build();

            voteOutboundOnly = PollVote.builder()
                    .voteId(UUID.randomUUID())
                    .poll(poll)
                    .student(student2)
                    .option(option)
                    .returnConfirmed(false)
                    .voteTime(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Deve listar todos os passageiros confirmados quando o sentido for IDA (OUTBOUND)")
        void shouldGetTravelPassengersSuccessfullyWhenDirectionIsOutbound() {
            travel.setDirection(PollDirection.OUTBOUND);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(pollVoteRepository.findAllWithStudentAndOptionByPollId(pollId))
                    .thenReturn(List.of(voteOutboundAndReturn, voteOutboundOnly));

            List<PollPassengerDTO> passengers = travelService.getTravelPassengers(travelId, busDriverUserId);

            assertNotNull(passengers);
            assertEquals(2, passengers.size());
            assertEquals("Ana Silva", passengers.get(0).studentName());
            assertEquals("Bruno Santos", passengers.get(1).studentName());
        }

        @Test
        @DisplayName("Deve listar apenas os passageiros com retorno confirmado quando o sentido for VOLTA (INBOUND)")
        void shouldGetTravelPassengersSuccessfullyWhenDirectionIsInbound() {
            travel.setDirection(PollDirection.INBOUND);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(pollVoteRepository.findAllWithStudentAndOptionByPollId(pollId))
                    .thenReturn(List.of(voteOutboundAndReturn, voteOutboundOnly));

            List<PollPassengerDTO> passengers = travelService.getTravelPassengers(travelId, busDriverUserId);

            assertNotNull(passengers);
            assertEquals(1, passengers.size());
            assertEquals("Ana Silva", passengers.get(0).studentName());
            assertTrue(passengers.get(0).returnConfirmed());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenTravelNotFound() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getTravelPassengers(travelId, busDriverUserId)
            );

            assertEquals("Viagem não encontrada.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException quando o usuário não tiver acesso")
        void shouldThrowCrossMunicipalityAccessExceptionWhenUserHasNoAccess() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            CrossMunicipalityAccessException exception = assertThrows(
                    CrossMunicipalityAccessException.class,
                    () -> travelService.getTravelPassengers(travelId, otherUserId)
            );

            assertEquals("Você não tem permissão para visualizar os dados desta viagem.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não possuir enquete vinculada")
        void shouldThrowObjectNotFoundExceptionWhenTravelHasNoPoll() {
            travel.setPoll(null);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.getTravelPassengers(travelId, busDriverUserId)
            );

            assertEquals("Esta viagem não possui uma enquete vinculada.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de startReturn (Início do Retorno)")
    class StartReturnTests {

        @Test
        @DisplayName("Deve iniciar retorno com sucesso alterando sentido para INBOUND")
        void shouldStartReturnSuccessfully() {
            travel.setDirection(PollDirection.OUTBOUND);
            travel.setStatus(TravelStatus.IN_PROGRESS);

            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelRepository.save(travel)).thenReturn(travel);
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.startReturn(travelId, busDriverUserId);

            assertNotNull(result);
            assertEquals(PollDirection.INBOUND, travel.getDirection());
            verify(travelRepository).save(travel);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenTravelNotFound() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.startReturn(travelId, busDriverUserId)
            );

            assertEquals("Viagem não encontrada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando o usuário não for o motorista responsável")
        void shouldThrowUserIsNotOwnerExceptionWhenDriverIsNotTheAssignedDriver() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> travelService.startReturn(travelId, otherUserId)
            );

            assertEquals("Você não tem permissão para alterar esta viagem pois não é o motorista responsável.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando a viagem não estiver em andamento")
        void shouldThrowIllegalArgumentExceptionWhenTravelIsNotInProgress() {
            travel.setStatus(TravelStatus.COMPLETED);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.startReturn(travelId, busDriverUserId)
            );

            assertEquals("Apenas viagens em andamento podem iniciar o retorno.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o retorno já foi iniciado")
        void shouldThrowIllegalArgumentExceptionWhenReturnAlreadyStarted() {
            travel.setDirection(PollDirection.INBOUND);
            travel.setStatus(TravelStatus.IN_PROGRESS);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.startReturn(travelId, busDriverUserId)
            );

            assertEquals("A viagem de retorno já foi iniciada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de completeTravel (Conclusão da Viagem)")
    class CompleteTravelTests {

        @Test
        @DisplayName("Deve concluir viagem com sucesso alterando status para COMPLETED")
        void shouldCompleteTravelSuccessfully() {
            travel.setStatus(TravelStatus.IN_PROGRESS);

            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelRepository.save(travel)).thenReturn(travel);
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.completeTravel(travelId, busDriverUserId);

            assertNotNull(result);
            assertEquals(TravelStatus.COMPLETED, travel.getStatus());
            verify(travelRepository).save(travel);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenTravelNotFound() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.completeTravel(travelId, busDriverUserId)
            );

            assertEquals("Viagem não encontrada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando o usuário não for o motorista responsável")
        void shouldThrowUserIsNotOwnerExceptionWhenDriverIsNotTheAssignedDriver() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> travelService.completeTravel(travelId, otherUserId)
            );

            assertEquals("Você não tem permissão para alterar esta viagem pois não é o motorista responsável.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando a viagem não estiver em andamento")
        void shouldThrowIllegalArgumentExceptionWhenTravelIsNotInProgress() {
            travel.setStatus(TravelStatus.COMPLETED);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.completeTravel(travelId, busDriverUserId)
            );

            assertEquals("Apenas viagens em andamento podem ser concluídas.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de cancelTravel (Cancelamento da Viagem)")
    class CancelTravelTests {

        @Test
        @DisplayName("Deve cancelar viagem com sucesso pelo motorista com motivo especificado")
        void shouldCancelTravelSuccessfullyByDriverWithReason() {
            travel.setStatus(TravelStatus.IN_PROGRESS);

            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelRepository.save(travel)).thenReturn(travel);
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.cancelTravel(travelId, busDriverUserId, "Problema mecânico");

            assertNotNull(result);
            assertEquals(TravelStatus.CANCELLED, travel.getStatus());
            assertEquals("Problema mecânico", travel.getCancellationReason());
            verify(travelRepository).save(travel);
        }

        @Test
        @DisplayName("Deve cancelar viagem com sucesso pela prefeitura aplicando motivo padrão quando vazio")
        void shouldCancelTravelSuccessfullyByMunicipalityWithDefaultReason() {
            travel.setStatus(TravelStatus.IN_PROGRESS);

            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));
            when(travelRepository.save(travel)).thenReturn(travel);
            when(travelMapper.toDTO(travel)).thenReturn(travelDTO);

            TravelDTO result = travelService.cancelTravel(travelId, municipalityUserId, "   ");

            assertNotNull(result);
            assertEquals(TravelStatus.CANCELLED, travel.getStatus());
            assertEquals("Viagem cancelada sem motivo especificado.", travel.getCancellationReason());
            verify(travelRepository).save(travel);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando a viagem não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenTravelNotFound() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> travelService.cancelTravel(travelId, busDriverUserId, "Motivo")
            );

            assertEquals("Viagem não encontrada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando usuário não for nem o motorista nem a prefeitura")
        void shouldThrowUserIsNotOwnerExceptionWhenUserHasNoCancellationPermission() {
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> travelService.cancelTravel(travelId, otherUserId, "Motivo")
            );

            assertEquals("Você não tem permissão para cancelar esta viagem.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando a viagem já estiver concluída")
        void shouldThrowIllegalArgumentExceptionWhenTravelIsAlreadyCompleted() {
            travel.setStatus(TravelStatus.COMPLETED);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.cancelTravel(travelId, busDriverUserId, "Motivo")
            );

            assertEquals("Não é possível cancelar uma viagem que já foi concluída ou cancelada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando a viagem já estiver cancelada")
        void shouldThrowIllegalArgumentExceptionWhenTravelIsAlreadyCancelled() {
            travel.setStatus(TravelStatus.CANCELLED);
            when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> travelService.cancelTravel(travelId, busDriverUserId, "Motivo")
            );

            assertEquals("Não é possível cancelar uma viagem que já foi concluída ou cancelada.", exception.getMessage());
            verify(travelRepository, never()).save(any());
        }
    }
}
