package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.CityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - CityController")
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private CityService cityService;

    private UserPrincipal createAdminPrincipal() {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email("admin@acaminho.io")
                .role(UserRole.ADMIN)
                .build());
    }

    private UserPrincipal createStudentPrincipal() {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email("student@ufpb.br")
                .role(UserRole.STUDENT)
                .build());
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/cities - Cadastra cidade com sucesso com role ADMIN (201 Created)")
        void shouldCreateCitySuccessfullyWhenAdmin() throws Exception {
            UUID cityId = UUID.randomUUID();
            CityRequestDTO requestDTO = new CityRequestDTO("João Pessoa", "Paraíba");
            CityResponseDTO responseDTO = new CityResponseDTO(cityId, "João Pessoa", "Paraíba");

            when(cityService.createCity(any(CityRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/cities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cityId").value(cityId.toString()))
                    .andExpect(jsonPath("$.cityName").value("João Pessoa"))
                    .andExpect(jsonPath("$.stateName").value("Paraíba"));

            verify(cityService).createCity(requestDTO);
        }

        @Test
        @DisplayName("GET /api/cities/{cityId} - Busca cidade por ID com sucesso (200 OK)")
        void shouldGetCityByIdSuccessfully() throws Exception {
            UUID cityId = UUID.randomUUID();
            CityResponseDTO responseDTO = new CityResponseDTO(cityId, "João Pessoa", "Paraíba");

            when(cityService.getCityById(cityId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/cities/{cityId}", cityId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cityId").value(cityId.toString()))
                    .andExpect(jsonPath("$.cityName").value("João Pessoa"))
                    .andExpect(jsonPath("$.stateName").value("Paraíba"));

            verify(cityService).getCityById(cityId);
        }

        @Test
        @DisplayName("GET /api/cities/state/{stateName} - Lista cidades do estado com sucesso (200 OK)")
        void shouldGetAllCitiesFromStateSuccessfully() throws Exception {
            UUID cityId = UUID.randomUUID();
            List<CityResponseDTO> cities = List.of(new CityResponseDTO(cityId, "João Pessoa", "Paraíba"));

            when(cityService.getAllCityFromStateByStateName("Paraíba")).thenReturn(cities);

            mockMvc.perform(get("/api/cities/state/Paraíba")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].cityName").value("João Pessoa"))
                    .andExpect(jsonPath("$[0].stateName").value("Paraíba"));

            verify(cityService).getAllCityFromStateByStateName("Paraíba");
        }

        @Test
        @DisplayName("DELETE /api/cities/{cityId} - Exclui cidade com sucesso com role ADMIN (204 No Content)")
        void shouldDeleteCitySuccessfullyWhenAdmin() throws Exception {
            UUID cityId = UUID.randomUUID();
            doNothing().when(cityService).deleteCity(cityId);

            mockMvc.perform(delete("/api/cities/{cityId}", cityId)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNoContent());

            verify(cityService).deleteCity(cityId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/cities - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateCityUnauthenticated() throws Exception {
            CityRequestDTO requestDTO = new CityRequestDTO("João Pessoa", "Paraíba");

            mockMvc.perform(post("/api/cities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/cities - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToCreateCity() throws Exception {
            CityRequestDTO requestDTO = new CityRequestDTO("João Pessoa", "Paraíba");

            mockMvc.perform(post("/api/cities")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/cities/{cityId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetCityByIdUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/cities/{cityId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/cities/state/{stateName} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllCitiesUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/cities/state/Paraíba"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/cities/{cityId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteCityUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/cities/{cityId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/cities/{cityId} - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToDeleteCity() throws Exception {
            UUID cityId = UUID.randomUUID();

            mockMvc.perform(delete("/api/cities/{cityId}", cityId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/cities - Falha com 400 Bad Request quando dados obrigatórios forem inválidos")
        void shouldReturnBadRequestWhenCityRequestDTOIsInvalid() throws Exception {
            CityRequestDTO requestDTO = new CityRequestDTO("", "");

            mockMvc.perform(post("/api/cities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/cities - Falha com 409 Conflict quando cidade já existir no mesmo estado")
        void shouldReturnConflictWhenCityAlreadyExists() throws Exception {
            CityRequestDTO requestDTO = new CityRequestDTO("João Pessoa", "Paraíba");
            when(cityService.createCity(any(CityRequestDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Cidade já existe."));

            mockMvc.perform(post("/api/cities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cidade já existe."));
        }

        @Test
        @DisplayName("POST /api/cities - Falha com 404 Not Found quando estado não for encontrado")
        void shouldReturnNotFoundWhenStateNotFoundOnCreateCity() throws Exception {
            CityRequestDTO requestDTO = new CityRequestDTO("João Pessoa", "Inexistente");
            when(cityService.createCity(any(CityRequestDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Estado não encontrado."));

            mockMvc.perform(post("/api/cities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Estado não encontrado."));
        }

        @Test
        @DisplayName("GET /api/cities/{cityId} - Falha com 404 Not Found quando cidade não existir")
        void shouldReturnNotFoundWhenCityNotFound() throws Exception {
            UUID cityId = UUID.randomUUID();
            when(cityService.getCityById(cityId))
                    .thenThrow(new ObjectNotFoundException("Cidade não encontrada"));

            mockMvc.perform(get("/api/cities/{cityId}", cityId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Cidade não encontrada"));
        }

        @Test
        @DisplayName("GET /api/cities/state/{stateName} - Falha com 404 Not Found quando estado não existir")
        void shouldReturnNotFoundWhenStateNotFound() throws Exception {
            when(cityService.getAllCityFromStateByStateName("Inexistente"))
                    .thenThrow(new ObjectNotFoundException("Estado não encontrado."));

            mockMvc.perform(get("/api/cities/state/Inexistente")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Estado não encontrado."));
        }

        @Test
        @DisplayName("DELETE /api/cities/{cityId} - Falha com 404 Not Found quando cidade não for encontrada")
        void shouldReturnNotFoundWhenCityNotFoundOnDelete() throws Exception {
            UUID cityId = UUID.randomUUID();
            doThrow(new ObjectNotFoundException("Cidade não encontrada"))
                    .when(cityService).deleteCity(cityId);

            mockMvc.perform(delete("/api/cities/{cityId}", cityId)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Cidade não encontrada"));
        }
    }
}
