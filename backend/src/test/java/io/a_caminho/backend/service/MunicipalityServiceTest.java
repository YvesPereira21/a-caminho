package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.dto.user.UserDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerAndAdminException;
import io.a_caminho.backend.mapper.MunicipalityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.CityRepository;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - MunicipalityService")
class MunicipalityServiceTest {

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private MunicipalityMapper municipalityMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MunicipalityService municipalityService;

    private UUID cityId;
    private UUID municipalityUserId;
    private User municipalityUser;
    private City city;
    private Municipality municipality;
    private MunicipalityRequestDTO requestDTO;
    private MunicipalityResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        cityId = UUID.randomUUID();
        municipalityUserId = UUID.randomUUID();

        State state = State.builder()
                .stateId(UUID.randomUUID())
                .stateName("Paraíba")
                .build();

        city = City.builder()
                .cityId(cityId)
                .cityName("João Pessoa")
                .state(state)
                .build();

        municipalityUser = User.builder()
                .userId(municipalityUserId)
                .email("prefeitura@joaopessoa.pb.gov.br")
                .password("encoded_pass")
                .role(UserRole.MUNICIPALITY)
                .build();

        municipality = Municipality.builder()
                .municipalityId(UUID.randomUUID())
                .municipalityName("Prefeitura de João Pessoa")
                .city(city)
                .user(municipalityUser)
                .build();

        UserDTO userDTO = new UserDTO("prefeitura@joaopessoa.pb.gov.br", "senha1234");
        requestDTO = new MunicipalityRequestDTO("Prefeitura de João Pessoa", userDTO, cityId);
        responseDTO = new MunicipalityResponseDTO("Prefeitura de João Pessoa");
    }

    @Nested
    @DisplayName("Cenários de createMunicipality (Criação de Prefeitura)")
    class CreateMunicipalityTests {

        @Test
        @DisplayName("Deve cadastrar uma prefeitura com sucesso")
        void shouldCreateMunicipalitySuccessfully() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(userRepository.existsByEmail("prefeitura@joaopessoa.pb.gov.br")).thenReturn(false);
            when(municipalityRepository.existsByMunicipalityName("Prefeitura de João Pessoa")).thenReturn(false);
            when(passwordEncoder.encode("senha1234")).thenReturn("encoded_pass");
            when(municipalityMapper.toEntity(requestDTO)).thenReturn(municipality);
            when(municipalityRepository.save(municipality)).thenReturn(municipality);
            when(municipalityMapper.toResponse(municipality)).thenReturn(responseDTO);

            MunicipalityResponseDTO result = municipalityService.createMunicipality(requestDTO);

            assertNotNull(result);
            assertEquals("Prefeitura de João Pessoa", result.municipalityName());
            verify(cityRepository).findById(cityId);
            verify(userRepository).existsByEmail("prefeitura@joaopessoa.pb.gov.br");
            verify(municipalityRepository).existsByMunicipalityName("Prefeitura de João Pessoa");
            verify(passwordEncoder).encode("senha1234");
            verify(municipalityRepository).save(municipality);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao cadastrar prefeitura com cidade inexistente")
        void shouldThrowObjectNotFoundExceptionWhenCityNotFoundOnCreate() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> municipalityService.createMunicipality(requestDTO));

            assertEquals("Cidade não encontrada.", ex.getMessage());
            verify(userRepository, never()).existsByEmail(any());
            verify(municipalityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException ao cadastrar prefeitura quando email já existir")
        void shouldThrowObjectAlreadyExistsExceptionWhenEmailAlreadyExistsOnCreate() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(userRepository.existsByEmail("prefeitura@joaopessoa.pb.gov.br")).thenReturn(true);

            ObjectAlreadyExistsException ex = assertThrows(ObjectAlreadyExistsException.class,
                    () -> municipalityService.createMunicipality(requestDTO));

            assertEquals("Usuário já existente.", ex.getMessage());
            verify(passwordEncoder, never()).encode(any());
            verify(municipalityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException ao cadastrar prefeitura quando nome da prefeitura já existir")
        void shouldThrowObjectAlreadyExistsExceptionWhenMunicipalityNameAlreadyExistsOnCreate() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(userRepository.existsByEmail("prefeitura@joaopessoa.pb.gov.br")).thenReturn(false);
            when(municipalityRepository.existsByMunicipalityName("Prefeitura de João Pessoa")).thenReturn(true);

            ObjectAlreadyExistsException ex = assertThrows(ObjectAlreadyExistsException.class,
                    () -> municipalityService.createMunicipality(requestDTO));

            assertEquals("Usuário já existente.", ex.getMessage());
            verify(passwordEncoder, never()).encode(any());
            verify(municipalityRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getMunicipalityByName (Busca de Prefeitura por Nome)")
    class GetMunicipalityByNameTests {

        @Test
        @DisplayName("Deve buscar prefeitura pelo nome com sucesso")
        void shouldGetMunicipalityByNameSuccessfully() {
            when(municipalityRepository.findByMunicipalityName("Prefeitura de João Pessoa"))
                    .thenReturn(Optional.of(municipality));
            when(municipalityMapper.toResponse(municipality)).thenReturn(responseDTO);

            MunicipalityResponseDTO result = municipalityService.getMunicipalityByName("Prefeitura de João Pessoa");

            assertNotNull(result);
            assertEquals("Prefeitura de João Pessoa", result.municipalityName());
            verify(municipalityRepository).findByMunicipalityName("Prefeitura de João Pessoa");
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao buscar prefeitura por nome inexistente")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnGetByName() {
            when(municipalityRepository.findByMunicipalityName("Inexistente"))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> municipalityService.getMunicipalityByName("Inexistente"));

            assertEquals("Prefeitura não encontrada.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllMunicipalityFromState (Listagem de Prefeituras por Estado)")
    class GetAllMunicipalityFromStateTests {

        @Test
        @DisplayName("Deve listar todas as prefeituras de um estado com sucesso")
        void shouldGetAllMunicipalityFromStateSuccessfully() {
            when(municipalityRepository.findAllMunicipalityFromStateByStateName("Paraíba"))
                    .thenReturn(List.of(municipality));
            when(municipalityMapper.toResponse(municipality)).thenReturn(responseDTO);

            List<MunicipalityResponseDTO> result = municipalityService.getAllMunicipalityFromState("Paraíba");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Prefeitura de João Pessoa", result.get(0).municipalityName());
            verify(municipalityRepository).findAllMunicipalityFromStateByStateName("Paraíba");
        }
    }

    @Nested
    @DisplayName("Cenários de deleteMunicipality (Exclusão de Prefeitura)")
    class DeleteMunicipalityTests {

        @Test
        @DisplayName("Deve excluir prefeitura com sucesso quando usuário for o proprietário")
        void shouldDeleteMunicipalitySuccessfullyWhenUserIsOwner() {
            when(userRepository.findById(municipalityUserId)).thenReturn(Optional.of(municipalityUser));
            when(municipalityRepository.findByMunicipalityName("Prefeitura de João Pessoa"))
                    .thenReturn(Optional.of(municipality));

            assertDoesNotThrow(() -> municipalityService.deleteMunicipality(municipalityUserId, "Prefeitura de João Pessoa"));

            verify(userRepository).findById(municipalityUserId);
            verify(municipalityRepository).findByMunicipalityName("Prefeitura de João Pessoa");
            verify(municipalityRepository).delete(municipality);
        }

        @Test
        @DisplayName("Deve excluir prefeitura com sucesso quando usuário for ADMIN")
        void shouldDeleteMunicipalitySuccessfullyWhenUserIsAdmin() {
            UUID adminId = UUID.randomUUID();
            User adminUser = User.builder()
                    .userId(adminId)
                    .email("admin@acaminho.io")
                    .role(UserRole.ADMIN)
                    .build();

            when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
            when(municipalityRepository.findByMunicipalityName("Prefeitura de João Pessoa"))
                    .thenReturn(Optional.of(municipality));

            assertDoesNotThrow(() -> municipalityService.deleteMunicipality(adminId, "Prefeitura de João Pessoa"));

            verify(userRepository).findById(adminId);
            verify(municipalityRepository).findByMunicipalityName("Prefeitura de João Pessoa");
            verify(municipalityRepository).delete(municipality);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao excluir prefeitura com usuário inexistente")
        void shouldThrowObjectNotFoundExceptionWhenUserNotFoundOnDelete() {
            UUID nonExistentUserId = UUID.randomUUID();
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> municipalityService.deleteMunicipality(nonExistentUserId, "Prefeitura de João Pessoa"));

            assertEquals("Usuário não existe", ex.getMessage());
            verify(municipalityRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao excluir prefeitura com nome inexistente")
        void shouldThrowObjectNotFoundExceptionWhenMunicipalityNotFoundOnDelete() {
            when(userRepository.findById(municipalityUserId)).thenReturn(Optional.of(municipalityUser));
            when(municipalityRepository.findByMunicipalityName("Inexistente"))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> municipalityService.deleteMunicipality(municipalityUserId, "Inexistente"));

            assertEquals("Prefeitura não encontrada.", ex.getMessage());
            verify(municipalityRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerAndAdminException ao excluir prefeitura quando usuário não for dono nem admin")
        void shouldThrowUserIsNotOwnerAndAdminExceptionWhenUserNotOwnerNorAdmin() {
            UUID otherUserId = UUID.randomUUID();
            User otherMunicipalityUser = User.builder()
                    .userId(otherUserId)
                    .email("outra@prefeitura.gov.br")
                    .role(UserRole.MUNICIPALITY)
                    .build();

            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherMunicipalityUser));
            when(municipalityRepository.findByMunicipalityName("Prefeitura de João Pessoa"))
                    .thenReturn(Optional.of(municipality));

            UserIsNotOwnerAndAdminException ex = assertThrows(UserIsNotOwnerAndAdminException.class,
                    () -> municipalityService.deleteMunicipality(otherUserId, "Prefeitura de João Pessoa"));

            assertEquals("Você não tem permissão para isso", ex.getMessage());
            verify(municipalityRepository, never()).delete(any());
        }
    }
}
