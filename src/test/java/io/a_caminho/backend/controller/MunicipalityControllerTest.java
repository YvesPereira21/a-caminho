package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.dto.user.UserDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerAndAdminException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.MunicipalityService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - MunicipalityController")
class MunicipalityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private MunicipalityService municipalityService;

    private UserPrincipal createAdminPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("admin@acaminho.io")
                .role(UserRole.ADMIN)
                .build());
    }

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@joaopessoa.pb.gov.br")
                .role(UserRole.MUNICIPALITY)
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
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("POST /api/municipalities - Cadastra prefeitura com sucesso como ADMIN (201 Created)")
        void shouldCreateMunicipalitySuccessfullyWhenAdmin() throws Exception {
            UUID cityId = UUID.randomUUID();
            UserDTO userDTO = new UserDTO("prefeitura@joaopessoa.pb.gov.br", "senha1234");
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("Prefeitura de João Pessoa", userDTO, cityId);
            MunicipalityResponseDTO responseDTO = new MunicipalityResponseDTO("Prefeitura de João Pessoa");

            when(municipalityService.createMunicipality(any(MunicipalityRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/municipalities")
                            .with(user(createAdminPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.municipalityName").value("Prefeitura de João Pessoa"));

            verify(municipalityService).createMunicipality(requestDTO);
        }

        @Test
        @DisplayName("GET /api/municipalities/{municipalityName} - Retorna prefeitura com sucesso como MUNICIPALITY (200 OK)")
        void shouldGetMunicipalityByNameSuccessfullyWhenMunicipalityRole() throws Exception {
            MunicipalityResponseDTO responseDTO = new MunicipalityResponseDTO("Prefeitura de João Pessoa");
            when(municipalityService.getMunicipalityByName("Prefeitura de João Pessoa")).thenReturn(responseDTO);

            mockMvc.perform(get("/api/municipalities/{municipalityName}", "Prefeitura de João Pessoa")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.municipalityName").value("Prefeitura de João Pessoa"));

            verify(municipalityService).getMunicipalityByName("Prefeitura de João Pessoa");
        }

        @Test
        @DisplayName("GET /api/municipalities/state/{stateName} - Retorna lista de prefeituras por estado com sucesso (200 OK)")
        void shouldGetAllMunicipalityFromStateSuccessfullyWhenAuthenticated() throws Exception {
            MunicipalityResponseDTO responseDTO = new MunicipalityResponseDTO("Prefeitura de João Pessoa");
            when(municipalityService.getAllMunicipalityFromState("Paraíba")).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/municipalities/state/{stateName}", "Paraíba")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].municipalityName").value("Prefeitura de João Pessoa"));

            verify(municipalityService).getAllMunicipalityFromState("Paraíba");
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Exclui prefeitura com sucesso como ADMIN (204 No Content)")
        void shouldDeleteMunicipalitySuccessfullyWhenAdmin() throws Exception {
            UUID adminId = UUID.randomUUID();
            doNothing().when(municipalityService).deleteMunicipality(adminId, "Prefeitura de João Pessoa");

            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Prefeitura de João Pessoa")
                            .with(user(createAdminPrincipal(adminId))))
                    .andExpect(status().isNoContent());

            verify(municipalityService).deleteMunicipality(adminId, "Prefeitura de João Pessoa");
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Exclui prefeitura com sucesso como MUNICIPALITY (204 No Content)")
        void shouldDeleteMunicipalitySuccessfullyWhenMunicipalityRole() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            doNothing().when(municipalityService).deleteMunicipality(municipalityUserId, "Prefeitura de João Pessoa");

            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Prefeitura de João Pessoa")
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isNoContent());

            verify(municipalityService).deleteMunicipality(municipalityUserId, "Prefeitura de João Pessoa");
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("POST /api/municipalities - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateMunicipalityUnauthenticated() throws Exception {
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("Prefeitura", new UserDTO("email@gov.br", "senha1234"), UUID.randomUUID());

            mockMvc.perform(post("/api/municipalities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/municipalities - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToCreateMunicipality() throws Exception {
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("Prefeitura", new UserDTO("email@gov.br", "senha1234"), UUID.randomUUID());

            mockMvc.perform(post("/api/municipalities")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/municipalities - Falha com 400 Bad Request quando dados obrigatórios forem inválidos")
        void shouldReturnBadRequestWhenCreateMunicipalityPayloadInvalid() throws Exception {
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("", null, null);

            mockMvc.perform(post("/api/municipalities")
                            .with(user(createAdminPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/municipalities - Falha com 404 Not Found quando cidade não for encontrada")
        void shouldReturnNotFoundWhenCityNotFoundOnCreateMunicipality() throws Exception {
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("Prefeitura", new UserDTO("email@gov.br", "senha1234"), UUID.randomUUID());
            when(municipalityService.createMunicipality(any(MunicipalityRequestDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Cidade não encontrada."));

            mockMvc.perform(post("/api/municipalities")
                            .with(user(createAdminPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Cidade não encontrada."));
        }

        @Test
        @DisplayName("POST /api/municipalities - Falha com 409 Conflict quando usuário já existir")
        void shouldReturnConflictWhenUserEmailAlreadyExistsOnCreateMunicipality() throws Exception {
            MunicipalityRequestDTO requestDTO = new MunicipalityRequestDTO("Prefeitura", new UserDTO("email@gov.br", "senha1234"), UUID.randomUUID());
            when(municipalityService.createMunicipality(any(MunicipalityRequestDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Usuário já existente."));

            mockMvc.perform(post("/api/municipalities")
                            .with(user(createAdminPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Usuário já existente."));
        }

        @Test
        @DisplayName("GET /api/municipalities/{municipalityName} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetMunicipalityByNameUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/municipalities/{municipalityName}", "Prefeitura"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/municipalities/{municipalityName} - Falha com 403 Forbidden quando usuário for ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToGetMunicipalityByName() throws Exception {
            mockMvc.perform(get("/api/municipalities/{municipalityName}", "Prefeitura")
                            .with(user(createAdminPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/municipalities/{municipalityName} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenStudentAttemptsToGetMunicipalityByName() throws Exception {
            mockMvc.perform(get("/api/municipalities/{municipalityName}", "Prefeitura")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/municipalities/{municipalityName} - Falha com 404 Not Found quando prefeitura não for encontrada")
        void shouldReturnNotFoundWhenMunicipalityNotFoundByName() throws Exception {
            when(municipalityService.getMunicipalityByName("Inexistente"))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não encontrada."));

            mockMvc.perform(get("/api/municipalities/{municipalityName}", "Inexistente")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Prefeitura não encontrada."));
        }

        @Test
        @DisplayName("GET /api/municipalities/state/{stateName} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllMunicipalitiesFromStateUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/municipalities/state/{stateName}", "Paraíba"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteMunicipalityUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Prefeitura"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenStudentAttemptsToDeleteMunicipality() throws Exception {
            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Prefeitura")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Falha com 403 Forbidden quando usuário não for dono nem admin")
        void shouldReturnForbiddenWhenUserNotOwnerNorAdminOnDelete() throws Exception {
            UUID userId = UUID.randomUUID();
            doThrow(new UserIsNotOwnerAndAdminException("Você não tem permissão para isso"))
                    .when(municipalityService).deleteMunicipality(userId, "Prefeitura");

            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Prefeitura")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Você não tem permissão para isso"));
        }

        @Test
        @DisplayName("DELETE /api/municipalities/{municipalityName} - Falha com 404 Not Found quando prefeitura não existir")
        void shouldReturnNotFoundWhenMunicipalityNotFoundOnDelete() throws Exception {
            UUID adminId = UUID.randomUUID();
            doThrow(new ObjectNotFoundException("Prefeitura não encontrada."))
                    .when(municipalityService).deleteMunicipality(adminId, "Inexistente");

            mockMvc.perform(delete("/api/municipalities/{municipalityName}", "Inexistente")
                            .with(user(createAdminPrincipal(adminId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Prefeitura não encontrada."));
        }
    }
}
