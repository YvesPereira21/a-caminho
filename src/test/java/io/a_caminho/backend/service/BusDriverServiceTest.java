package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.user.UserDTO;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.BusDriverMapper;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - BusDriverService")
class BusDriverServiceTest {

    @Mock
    private BusDriverRepository busDriverRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private BusDriverMapper busDriverMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BusDriverService busDriverService;

    private UUID municipalityUserId;
    private UUID busDriverId;
    private Municipality municipality;
    private BusDriver busDriver;
    private BusDriverRequestDTO requestDTO;
    private BusDriverResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        municipalityUserId = UUID.randomUUID();
        busDriverId = UUID.randomUUID();

        User municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@cidade.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(UUID.randomUUID())
                .municipalityName("Prefeitura Municipal")
                .user(municipalityUser)
                .build();

        User driverUser = User.builder()
                .userId(UUID.randomUUID())
                .email("motorista@cidade.gov.br")
                .password("encoded_pass")
                .role(UserRole.BUS_DRIVER)
                .build();

        busDriver = BusDriver.builder()
                .busDriverId(busDriverId)
                .busDriverName("Sebastião da Silva")
                .user(driverUser)
                .municipality(municipality)
                .build();

        UserDTO userDTO = new UserDTO("motorista@cidade.gov.br", "senha1234");
        requestDTO = new BusDriverRequestDTO("Sebastião da Silva", userDTO);
        responseDTO = new BusDriverResponseDTO(busDriverId, "Sebastião da Silva", "motorista@cidade.gov.br");
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("Deve cadastrar motorista com sucesso associado à prefeitura autenticada")
        void shouldCreateBusDriverAccountSuccessfully() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.of(municipality));
            when(userRepository.existsByEmail("motorista@cidade.gov.br")).thenReturn(false);
            when(passwordEncoder.encode("senha1234")).thenReturn("encoded_pass");
            when(busDriverMapper.toEntity(requestDTO)).thenReturn(busDriver);
            when(busDriverRepository.save(busDriver)).thenReturn(busDriver);
            when(busDriverMapper.toResponse(busDriver)).thenReturn(responseDTO);

            BusDriverResponseDTO result = busDriverService.createBusDriverAccount(requestDTO, municipalityUserId);

            assertNotNull(result);
            assertEquals("Sebastião da Silva", result.busDriverName());
            assertEquals("motorista@cidade.gov.br", result.email());
            verify(municipalityRepository).findByUser_UserId(municipalityUserId);
            verify(userRepository).existsByEmail("motorista@cidade.gov.br");
            verify(passwordEncoder).encode("senha1234");
            verify(busDriverRepository).save(busDriver);
        }

        @Test
        @DisplayName("Deve buscar motorista por ID com sucesso para a prefeitura autenticada")
        void shouldGetBusDriverByIdSuccessfully() {
            when(busDriverRepository.findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId))
                    .thenReturn(Optional.of(busDriver));
            when(busDriverMapper.toResponse(busDriver)).thenReturn(responseDTO);

            BusDriverResponseDTO result = busDriverService.getBusDriverById(busDriverId, municipalityUserId);

            assertNotNull(result);
            assertEquals(busDriverId, result.busDriverId());
            assertEquals("Sebastião da Silva", result.busDriverName());
            verify(busDriverRepository).findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId);
        }

        @Test
        @DisplayName("Deve listar todos os motoristas pertencentes à prefeitura autenticada com sucesso")
        void shouldGetAllBusDriversFromMunicipalitySuccessfully() {
            when(busDriverRepository.findAllByMunicipality_User_UserId(municipalityUserId))
                    .thenReturn(List.of(busDriver));
            when(busDriverMapper.toResponse(busDriver)).thenReturn(responseDTO);

            List<BusDriverResponseDTO> result = busDriverService.getAllBusDriversFromMunicipality(municipalityUserId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Sebastião da Silva", result.get(0).busDriverName());
            verify(busDriverRepository).findAllByMunicipality_User_UserId(municipalityUserId);
        }

        @Test
        @DisplayName("Deve excluir motorista da prefeitura autenticada com sucesso")
        void shouldDeleteBusDriverAccountSuccessfully() {
            when(busDriverRepository.findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId))
                    .thenReturn(Optional.of(busDriver));

            assertDoesNotThrow(() -> busDriverService.deleteBusDriverAccount(busDriverId, municipalityUserId));

            verify(busDriverRepository).findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId);
            verify(busDriverRepository).delete(busDriver);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando prefeitura não for encontrada pelo usuário")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnCreate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> busDriverService.createBusDriverAccount(requestDTO, municipalityUserId));

            assertEquals("Prefeitura não existe.", ex.getMessage());
            verify(userRepository, never()).existsByEmail(any());
            verify(busDriverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando e-mail do motorista já existir")
        void shouldThrowObjectAlreadyExistsExceptionWhenEmailAlreadyExistsOnCreate() {
            when(municipalityRepository.findByUser_UserId(municipalityUserId)).thenReturn(Optional.of(municipality));
            when(userRepository.existsByEmail("motorista@cidade.gov.br")).thenReturn(true);

            ObjectAlreadyExistsException ex = assertThrows(ObjectAlreadyExistsException.class,
                    () -> busDriverService.createBusDriverAccount(requestDTO, municipalityUserId));

            assertEquals("Usuário já existente.", ex.getMessage());
            verify(passwordEncoder, never()).encode(any());
            verify(busDriverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException ao buscar motorista que não pertence à prefeitura")
        void shouldThrowCrossMunicipalityAccessExceptionWhenBusDriverNotFoundOnGet() {
            when(busDriverRepository.findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId))
                    .thenReturn(Optional.empty());

            CrossMunicipalityAccessException ex = assertThrows(CrossMunicipalityAccessException.class,
                    () -> busDriverService.getBusDriverById(busDriverId, municipalityUserId));

            assertEquals("Motorista não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar CrossMunicipalityAccessException ao excluir motorista que não pertence à prefeitura")
        void shouldThrowCrossMunicipalityAccessExceptionWhenBusDriverNotFoundOnDelete() {
            when(busDriverRepository.findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId))
                    .thenReturn(Optional.empty());

            CrossMunicipalityAccessException ex = assertThrows(CrossMunicipalityAccessException.class,
                    () -> busDriverService.deleteBusDriverAccount(busDriverId, municipalityUserId));

            assertEquals("Motorista não encontrado.", ex.getMessage());
            verify(busDriverRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException ao excluir motorista se a prefeitura não for a do usuário autenticado")
        void shouldThrowUserIsNotOwnerExceptionWhenMunicipalityUserMismatchOnDelete() {
            UUID differentUserId = UUID.randomUUID();
            when(busDriverRepository.findByBusDriverIdAndMunicipality_User_UserId(busDriverId, differentUserId))
                    .thenReturn(Optional.of(busDriver));

            UserIsNotOwnerException ex = assertThrows(UserIsNotOwnerException.class,
                    () -> busDriverService.deleteBusDriverAccount(busDriverId, differentUserId));

            assertEquals("Você não tem permissão para isso", ex.getMessage());
            verify(busDriverRepository, never()).delete(any());
        }
    }
}
