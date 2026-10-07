package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.mapper.UniversityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.repository.CityRepository;
import io.a_caminho.backend.repository.UniversityRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("Testes Unitários - UniversityService")
class UniversityServiceTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversityMapper universityMapper;

    @InjectMocks
    private UniversityService universityService;

    private UUID universityId;
    private City city;
    private University university;
    private UniversityRequestDTO requestDTO;
    private UniversityResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        universityId = UUID.randomUUID();

        State state = State.builder()
                .stateId(UUID.randomUUID())
                .stateName("Paraíba")
                .build();

        city = City.builder()
                .cityId(UUID.randomUUID())
                .cityName("João Pessoa")
                .state(state)
                .build();

        university = University.builder()
                .universityId(universityId)
                .name("Universidade Federal da Paraíba")
                .campus("Campus I")
                .city(city)
                .build();

        requestDTO = new UniversityRequestDTO("Universidade Federal da Paraíba", "Campus I", "João Pessoa", "Paraíba");
        responseDTO = new UniversityResponseDTO(universityId, "Universidade Federal da Paraíba", "Campus I", "João Pessoa", "Paraíba");
    }

    @Nested
    @DisplayName("Cenários de createUniversity (Cadastro de Universidade)")
    class CreateUniversityTests {

        @Test
        @DisplayName("Deve cadastrar uma universidade com sucesso")
        void shouldCreateUniversitySuccessfully() {
            when(cityRepository.findByCityNameAndState_StateName("João Pessoa", "Paraíba"))
                    .thenReturn(Optional.of(city));
            when(universityMapper.toEntity(requestDTO)).thenReturn(university);
            when(universityRepository.save(university)).thenReturn(university);
            when(universityMapper.toResponse(university)).thenReturn(responseDTO);

            UniversityResponseDTO result = universityService.createUniversity(requestDTO);

            assertNotNull(result);
            assertEquals(universityId, result.id());
            assertEquals("Campus I", result.campus());
            assertEquals("João Pessoa", result.cityName());
            assertEquals("Paraíba", result.stateName());

            verify(cityRepository).findByCityNameAndState_StateName("João Pessoa", "Paraíba");
            verify(universityMapper).toEntity(requestDTO);
            verify(universityRepository).save(university);
            verify(universityMapper).toResponse(university);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao cadastrar universidade quando cidade não for encontrada")
        void shouldThrowObjectNotFoundExceptionWhenCityNotFoundOnCreate() {
            when(cityRepository.findByCityNameAndState_StateName("João Pessoa", "Paraíba"))
                    .thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> universityService.createUniversity(requestDTO));

            assertEquals("Cidade não encontrada.", ex.getMessage());
            verify(universityRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getAllUniversityByNameFromState (Listagem de Universidades por Estado)")
    class GetAllUniversityByNameFromStateTests {

        @Test
        @DisplayName("Deve listar universidades por nome e estado com sucesso")
        void shouldGetAllUniversityByNameFromStateSuccessfully() {
            when(universityRepository.findAllByCampusContainingIgnoreCaseAndCity_State_StateName("Campus I", "Paraíba"))
                    .thenReturn(List.of(university));
            when(universityMapper.toResponse(university)).thenReturn(responseDTO);

            List<UniversityResponseDTO> result = universityService.getAllUniversityByNameFromState("Campus I", "Paraíba");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(universityId, result.get(0).id());
            assertEquals("Campus I", result.get(0).campus());

            verify(universityRepository).findAllByCampusContainingIgnoreCaseAndCity_State_StateName("Campus I", "Paraíba");
        }
    }

    @Nested
    @DisplayName("Cenários de deleteUniversity (Exclusão de Universidade)")
    class DeleteUniversityTests {

        @Test
        @DisplayName("Deve excluir universidade por ID com sucesso")
        void shouldDeleteUniversitySuccessfully() {
            when(universityRepository.findById(universityId)).thenReturn(Optional.of(university));

            assertDoesNotThrow(() -> universityService.deleteUniversity(universityId));

            verify(universityRepository).findById(universityId);
            verify(universityRepository).delete(university);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao excluir universidade quando ID não existir")
        void shouldThrowObjectNotFoundExceptionWhenUniversityNotFoundOnDelete() {
            when(universityRepository.findById(universityId)).thenReturn(Optional.empty());

            ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                    () -> universityService.deleteUniversity(universityId));

            assertEquals("Universidade não encontrada.", ex.getMessage());
            verify(universityRepository, never()).delete(any());
        }
    }
}
