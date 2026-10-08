package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.BusMapper;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.BusRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - BusService")
class BusServiceTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private BusMapper busMapper;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private BusDriverRepository busDriverRepository;

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private BusService busService;

    private UUID municipalityUserId;
    private UUID otherUserId;
    private UUID municipalityId;
    private UUID busDriverUserId;
    private UUID busId;

    private User municipalityUser;
    private User otherUser;
    private Municipality municipality;
    private Municipality otherMunicipality;
    private BusDriver busDriver;
    private Bus bus;
    private BusCreateDTO busCreateDTO;
    private BusUpdateDTO busUpdateDTO;
    private BusDTO busDTO;

    @BeforeEach
    void setUp() {
        municipalityUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        municipalityId = UUID.randomUUID();
        busDriverUserId = UUID.randomUUID();
        busId = UUID.randomUUID();

        municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        otherUser = User.builder()
                .userId(otherUserId)
                .email("outra.prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .municipalityName("Prefeitura de João Pessoa")
                .user(municipalityUser)
                .build();

        otherMunicipality = Municipality.builder()
                .municipalityId(UUID.randomUUID())
                .municipalityName("Prefeitura de Campina Grande")
                .user(otherUser)
                .build();

        busDriver = BusDriver.builder()
                .busDriverId(UUID.randomUUID())
                .busDriverName("Sebastião da Silva")
                .municipality(municipality)
                .build();

        bus = Bus.builder()
                .busId(busId)
                .busName("Ônibus 01")
                .seatsQuantity(44)
                .municipality(municipality)
                .build();

        busCreateDTO = new BusCreateDTO("Ônibus 01", 44);
        busUpdateDTO = new BusUpdateDTO("Ônibus 01 Atualizado", 50);
        busDTO = new BusDTO(busId, "Ônibus 01", 44);
    }

    @Nested
    @DisplayName("Cenários de createBus")
    class CreateBusTests {

        @Test
        @DisplayName("Deve cadastrar ônibus com sucesso quando dados válidos")
        void shouldCreateBusSuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                    municipalityId, busCreateDTO.busName()))
                    .thenReturn(false);
            when(busMapper.toEntity(busCreateDTO)).thenReturn(bus);
            when(busRepository.save(bus)).thenReturn(bus);
            when(busMapper.toDTO(bus)).thenReturn(busDTO);

            BusDTO result = busService.createBus(municipalityUserId, busCreateDTO);

            assertNotNull(result);
            assertEquals(busId, result.busId());
            assertEquals("Ônibus 01", result.busName());
            assertEquals(44, result.seatsQuantity());
            verify(busRepository).save(bus);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.createBus(municipalityUserId, busCreateDTO)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando ônibus com mesmo nome já existir no município")
        void shouldThrowObjectAlreadyExistsExceptionWhenBusNameAlreadyExists() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                    municipalityId, busCreateDTO.busName()))
                    .thenReturn(true);

            ObjectAlreadyExistsException exception = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> busService.createBus(municipalityUserId, busCreateDTO)
            );

            assertEquals("Já existe um ônibus cadastrado com este nome.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getBusById")
    class GetBusByIdTests {

        @Test
        @DisplayName("Deve retornar ônibus com sucesso quando prefeitura for a proprietária")
        void shouldReturnBusWhenMunicipalityIsOwner() {
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(busMapper.toDTO(bus)).thenReturn(busDTO);

            BusDTO result = busService.getBusById(municipalityUserId, busId);

            assertNotNull(result);
            assertEquals(busId, result.busId());
            assertEquals("Ônibus 01", result.busName());
            verify(busRepository).findById(busId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ônibus não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenBusNotFound() {
            when(busRepository.findById(busId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.getBusById(municipalityUserId, busId)
            );

            assertEquals("Ônibus não encontrado.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando prefeitura não for a proprietária")
        void shouldThrowUserIsNotOwnerExceptionWhenNotOwner() {
            Bus otherBus = Bus.builder()
                    .busId(busId)
                    .busName("Ônibus de Outro Município")
                    .municipality(otherMunicipality)
                    .build();

            when(busRepository.findById(busId)).thenReturn(Optional.of(otherBus));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> busService.getBusById(municipalityUserId, busId)
            );

            assertEquals("Você não tem permissão para acessar este recurso.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllBusesByMunicipality")
    class GetAllBusesByMunicipalityTests {

        @Test
        @DisplayName("Deve listar todos os ônibus da prefeitura com sucesso")
        void shouldGetAllBusesByMunicipalitySuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(List.of(bus));
            when(busMapper.toDTO(bus)).thenReturn(busDTO);

            List<BusDTO> result = busService.getAllBusesByMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Ônibus 01", result.get(0).busName());
            verify(busRepository).findAllByMunicipality_User_UserId(municipalityUserId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada ao listar")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnList() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.getAllBusesByMunicipality(municipalityUserId)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
            verify(busRepository, never()).findAllByMunicipality_User_UserId(any());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando município não possuir ônibus cadastrados")
        void shouldReturnEmptyListWhenNoBusesFound() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(Collections.emptyList());

            List<BusDTO> result = busService.getAllBusesByMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllBusesForDriver")
    class GetAllBusesForDriverTests {

        @Test
        @DisplayName("Deve listar ônibus para o motorista com sucesso")
        void shouldGetAllBusesForDriverSuccessfully() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId))
                    .thenReturn(Optional.of(busDriver));
            when(busRepository.findAllByMunicipality_MunicipalityId(municipalityId))
                    .thenReturn(List.of(bus));
            when(busMapper.toDTO(bus)).thenReturn(busDTO);

            List<BusDTO> result = busService.getAllBusesForDriver(busDriverUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Ônibus 01", result.get(0).busName());
            verify(busRepository).findAllByMunicipality_MunicipalityId(municipalityId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando motorista não for encontrado")
        void shouldThrowObjectNotFoundExceptionWhenDriverNotFound() {
            when(busDriverRepository.findByUser_UserId(busDriverUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.getAllBusesForDriver(busDriverUserId)
            );

            assertEquals("Motorista não encontrado.", exception.getMessage());
            verify(busRepository, never()).findAllByMunicipality_MunicipalityId(any());
        }
    }

    @Nested
    @DisplayName("Cenários de updateBus")
    class UpdateBusTests {

        @Test
        @DisplayName("Deve atualizar ônibus com sucesso quando dados válidos")
        void shouldUpdateBusSuccessfully() {
            Bus updatedBus = Bus.builder()
                    .busId(busId)
                    .busName("Ônibus 01 Atualizado")
                    .seatsQuantity(50)
                    .municipality(municipality)
                    .build();
            BusDTO updatedBusDTO = new BusDTO(busId, "Ônibus 01 Atualizado", 50);

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                    municipalityId, busUpdateDTO.busName()))
                    .thenReturn(false);
            when(busMapper.updateEntityFromDTO(busUpdateDTO, bus)).thenReturn(updatedBus);
            when(busRepository.save(updatedBus)).thenReturn(updatedBus);
            when(busMapper.toDTO(updatedBus)).thenReturn(updatedBusDTO);

            BusDTO result = busService.updateBus(municipalityUserId, busId, busUpdateDTO);

            assertNotNull(result);
            assertEquals("Ônibus 01 Atualizado", result.busName());
            assertEquals(50, result.seatsQuantity());
            verify(busRepository).save(updatedBus);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada ao atualizar")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnUpdate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.updateBus(municipalityUserId, busId, busUpdateDTO)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ônibus não for encontrado ao atualizar")
        void shouldThrowObjectNotFoundExceptionWhenBusNotFoundOnUpdate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.updateBus(municipalityUserId, busId, busUpdateDTO)
            );

            assertEquals("Ônibus não encontrado.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando prefeitura não for dona do ônibus ao atualizar")
        void shouldThrowUserIsNotOwnerExceptionWhenNotOwnerOnUpdate() {
            Bus otherBus = Bus.builder()
                    .busId(busId)
                    .busName("Ônibus 01")
                    .municipality(otherMunicipality)
                    .build();

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(otherBus));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> busService.updateBus(municipalityUserId, busId, busUpdateDTO)
            );

            assertEquals("Você não tem permissão para acessar este recurso.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando novo nome de ônibus já existir")
        void shouldThrowObjectAlreadyExistsExceptionWhenNewBusNameAlreadyExists() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(busRepository.existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(
                    municipalityId, busUpdateDTO.busName()))
                    .thenReturn(true);

            ObjectAlreadyExistsException exception = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> busService.updateBus(municipalityUserId, busId, busUpdateDTO)
            );

            assertEquals("Já existe um ônibus cadastrado com este nome.", exception.getMessage());
            verify(busRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve verificar duplicidade se o nome não for alterado")
        void shouldNotCheckDuplicateIfNameUnchanged() {
            BusUpdateDTO sameNameDTO = new BusUpdateDTO("Ônibus 01", 50);

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(busMapper.updateEntityFromDTO(sameNameDTO, bus)).thenReturn(bus);
            when(busRepository.save(bus)).thenReturn(bus);
            when(busMapper.toDTO(bus)).thenReturn(busDTO);

            busService.updateBus(municipalityUserId, busId, sameNameDTO);

            verify(busRepository, never()).existsByMunicipality_MunicipalityIdAndBusNameIgnoreCase(any(), any());
            verify(busRepository).save(bus);
        }
    }

    @Nested
    @DisplayName("Cenários de deleteBus")
    class DeleteBusTests {

        @Test
        @DisplayName("Deve excluir ônibus com sucesso quando não houver viagens vinculadas")
        void shouldDeleteBusSuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(travelRepository.existsByBus_BusId(busId)).thenReturn(false);

            busService.deleteBus(municipalityUserId, busId);

            verify(busRepository).delete(bus);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada ao excluir")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnDelete() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.deleteBus(municipalityUserId, busId)
            );

            assertEquals("Prefeitura não encontrada.", exception.getMessage());
            verify(busRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando ônibus não for encontrado ao excluir")
        void shouldThrowObjectNotFoundExceptionWhenBusNotFoundOnDelete() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.empty());

            ObjectNotFoundException exception = assertThrows(
                    ObjectNotFoundException.class,
                    () -> busService.deleteBus(municipalityUserId, busId)
            );

            assertEquals("Ônibus não encontrado.", exception.getMessage());
            verify(busRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException quando prefeitura não for a dona ao excluir")
        void shouldThrowUserIsNotOwnerExceptionWhenNotOwnerOnDelete() {
            Bus otherBus = Bus.builder()
                    .busId(busId)
                    .busName("Ônibus 01")
                    .municipality(otherMunicipality)
                    .build();

            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(otherBus));

            UserIsNotOwnerException exception = assertThrows(
                    UserIsNotOwnerException.class,
                    () -> busService.deleteBus(municipalityUserId, busId)
            );

            assertEquals("Você não tem permissão para acessar este recurso.", exception.getMessage());
            verify(busRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando existirem viagens vinculadas ao ônibus")
        void shouldThrowIllegalArgumentExceptionWhenBusHasTravelsOnDelete() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId))
                    .thenReturn(Optional.of(municipality));
            when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
            when(travelRepository.existsByBus_BusId(busId)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> busService.deleteBus(municipalityUserId, busId)
            );

            assertEquals("Não é possível excluir este ônibus pois já existem viagens vinculadas a ele.", exception.getMessage());
            verify(busRepository, never()).delete(any());
        }
    }
}
