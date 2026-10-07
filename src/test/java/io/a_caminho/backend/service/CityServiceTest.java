package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.mapper.CityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.repository.CityRepository;
import io.a_caminho.backend.repository.StateRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - CityService")
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityService cityService;

    private State state;
    private City city;
    private CityRequestDTO requestDTO;
    private CityResponseDTO responseDTO;
    private UUID cityId;

    @BeforeEach
    void setUp() {
        cityId = UUID.randomUUID();
        state = State.builder()
                .stateId(UUID.randomUUID())
                .stateName("Paraíba")
                .build();

        city = City.builder()
                .cityId(cityId)
                .cityName("João Pessoa")
                .state(state)
                .build();

        requestDTO = new CityRequestDTO("João Pessoa", "Paraíba");
        responseDTO = new CityResponseDTO(cityId, "João Pessoa", "Paraíba");
    }

    @Nested
    @DisplayName("Cenários de createCity (Criação de Cidade)")
    class CreateCityTests {

        @Test
        @DisplayName("Deve cadastrar uma cidade com sucesso quando estado existir e cidade não for duplicada")
        void shouldCreateCitySuccessfully() {
            when(cityRepository.existsByCityNameAndState_StateName("João Pessoa", "Paraíba")).thenReturn(false);
            when(stateRepository.findByStateName("Paraíba")).thenReturn(Optional.of(state));
            when(cityMapper.toEntity(requestDTO)).thenReturn(city);
            when(cityRepository.save(city)).thenReturn(city);
            when(cityMapper.toResponse(city)).thenReturn(responseDTO);

            CityResponseDTO result = cityService.createCity(requestDTO);

            assertNotNull(result);
            assertEquals("João Pessoa", result.cityName());
            assertEquals("Paraíba", result.stateName());
            verify(cityRepository).save(city);
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException quando cidade já existir no mesmo estado")
        void shouldThrowObjectAlreadyExistsExceptionWhenCityAlreadyExistsInState() {
            when(cityRepository.existsByCityNameAndState_StateName("João Pessoa", "Paraíba")).thenReturn(true);

            assertThrows(ObjectAlreadyExistsException.class, () -> cityService.createCity(requestDTO));
            verify(stateRepository, never()).findByStateName(any());
            verify(cityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estado não for encontrado ao criar cidade")
        void shouldThrowObjectNotFoundExceptionWhenStateNotFoundOnCreateCity() {
            when(cityRepository.existsByCityNameAndState_StateName("João Pessoa", "Paraíba")).thenReturn(false);
            when(stateRepository.findByStateName("Paraíba")).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class, () -> cityService.createCity(requestDTO));
            verify(cityRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getCityById (Busca de Cidade por ID)")
    class GetCityByIdTests {

        @Test
        @DisplayName("Deve buscar cidade por ID com sucesso")
        void shouldGetCityByIdSuccessfully() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(cityMapper.toResponse(city)).thenReturn(responseDTO);

            CityResponseDTO result = cityService.getCityById(cityId);

            assertNotNull(result);
            assertEquals(cityId, result.cityId());
            assertEquals("João Pessoa", result.cityName());
            verify(cityRepository).findById(cityId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando cidade não for encontrada por ID")
        void shouldThrowObjectNotFoundExceptionWhenCityNotFoundOnGetById() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class, () -> cityService.getCityById(cityId));
        }
    }

    @Nested
    @DisplayName("Cenários de getAllCityFromStateByStateName (Listagem de Cidades por Estado)")
    class GetAllCityFromStateByStateNameTests {

        @Test
        @DisplayName("Deve listar todas as cidades de um estado com sucesso")
        void shouldGetAllCitiesFromStateByStateNameSuccessfully() {
            when(stateRepository.findByStateName("Paraíba")).thenReturn(Optional.of(state));
            when(cityRepository.findAllByState_StateName("Paraíba")).thenReturn(List.of(city));
            when(cityMapper.toResponse(city)).thenReturn(responseDTO);

            List<CityResponseDTO> result = cityService.getAllCityFromStateByStateName("Paraíba");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("João Pessoa", result.get(0).cityName());
            verify(stateRepository).findByStateName("Paraíba");
            verify(cityRepository).findAllByState_StateName("Paraíba");
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando estado não for encontrado ao listar cidades")
        void shouldThrowObjectNotFoundExceptionWhenStateNotFoundOnGetAllCities() {
            when(stateRepository.findByStateName("Inexistente")).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class, () -> cityService.getAllCityFromStateByStateName("Inexistente"));
            verify(cityRepository, never()).findAllByState_StateName(any());
        }
    }

    @Nested
    @DisplayName("Cenários de deleteCity (Exclusão de Cidade)")
    class DeleteCityTests {

        @Test
        @DisplayName("Deve excluir cidade com sucesso quando cidade for encontrada")
        void shouldDeleteCitySuccessfully() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));

            cityService.deleteCity(cityId);

            verify(cityRepository).findById(cityId);
            verify(cityRepository).delete(city);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException quando cidade não for encontrada ao excluir")
        void shouldThrowObjectNotFoundExceptionWhenCityNotFoundOnDeleteCity() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class, () -> cityService.deleteCity(cityId));
            verify(cityRepository, never()).delete(any());
        }
    }
}
