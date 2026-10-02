package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.UniversityService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - UniversityController")
class UniversityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UniversityService universityService;

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
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("POST /api/universities - Cadastra universidade com sucesso como ADMIN (201 Created)")
        void shouldCreateUniversitySuccessfullyWhenAdmin() throws Exception {
            UUID universityId = UUID.randomUUID();
            UniversityRequestDTO requestDTO = new UniversityRequestDTO("Universidade Federal da Paraíba", "Campus I", "João Pessoa", "Paraíba");
            UniversityResponseDTO responseDTO = new UniversityResponseDTO(universityId, "Campus I", "João Pessoa", "Paraíba");

            when(universityService.createUniversity(any(UniversityRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/universities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(universityId.toString()))
                    .andExpect(jsonPath("$.campus").value("Campus I"))
                    .andExpect(jsonPath("$.cityName").value("João Pessoa"))
                    .andExpect(jsonPath("$.stateName").value("Paraíba"));

            verify(universityService).createUniversity(requestDTO);
        }

        @Test
        @DisplayName("GET /api/universities - Lista universidades por nome e estado com sucesso (200 OK)")
        void shouldGetAllUniversityByNameFromStateSuccessfullyWhenAuthenticated() throws Exception {
            UUID universityId = UUID.randomUUID();
            UniversityResponseDTO responseDTO = new UniversityResponseDTO(universityId, "Campus I", "João Pessoa", "Paraíba");

            when(universityService.getAllUniversityByNameFromState("Campus I", "Paraíba"))
                    .thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/universities")
                            .param("universityName", "Campus I")
                            .param("stateName", "Paraíba")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(universityId.toString()))
                    .andExpect(jsonPath("$[0].campus").value("Campus I"))
                    .andExpect(jsonPath("$[0].cityName").value("João Pessoa"))
                    .andExpect(jsonPath("$[0].stateName").value("Paraíba"));

            verify(universityService).getAllUniversityByNameFromState("Campus I", "Paraíba");
        }

        @Test
        @DisplayName("DELETE /api/universities/{universityId} - Exclui universidade com sucesso como ADMIN (204 No Content)")
        void shouldDeleteUniversitySuccessfullyWhenAdmin() throws Exception {
            UUID universityId = UUID.randomUUID();
            doNothing().when(universityService).deleteUniversity(universityId);

            mockMvc.perform(delete("/api/universities/{universityId}", universityId)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNoContent());

            verify(universityService).deleteUniversity(universityId);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("POST /api/universities - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateUniversityUnauthenticated() throws Exception {
            UniversityRequestDTO requestDTO = new UniversityRequestDTO("UFPB", "Campus I", "João Pessoa", "Paraíba");

            mockMvc.perform(post("/api/universities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/universities - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToCreateUniversity() throws Exception {
            UniversityRequestDTO requestDTO = new UniversityRequestDTO("UFPB", "Campus I", "João Pessoa", "Paraíba");

            mockMvc.perform(post("/api/universities")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/universities - Falha com 400 Bad Request quando campos obrigatórios forem inválidos")
        void shouldReturnBadRequestWhenCreateUniversityPayloadIsInvalid() throws Exception {
            UniversityRequestDTO requestDTO = new UniversityRequestDTO("", "Campus I", "", "");

            mockMvc.perform(post("/api/universities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/universities - Falha com 404 Not Found quando cidade não for encontrada")
        void shouldReturnNotFoundWhenCityNotFoundOnCreateUniversity() throws Exception {
            UniversityRequestDTO requestDTO = new UniversityRequestDTO("UFPB", "Campus I", "Inexistente", "Paraíba");
            when(universityService.createUniversity(any(UniversityRequestDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Cidade não encontrada."));

            mockMvc.perform(post("/api/universities")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Cidade não encontrada."));
        }

        @Test
        @DisplayName("GET /api/universities - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllUniversitiesUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/universities")
                            .param("universityName", "UFPB")
                            .param("stateName", "Paraíba"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/universities/{universityId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteUniversityUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/universities/{universityId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/universities/{universityId} - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToDeleteUniversity() throws Exception {
            mockMvc.perform(delete("/api/universities/{universityId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/universities/{universityId} - Falha com 404 Not Found quando universidade não existir")
        void shouldReturnNotFoundWhenUniversityNotFoundOnDelete() throws Exception {
            UUID universityId = UUID.randomUUID();
            doThrow(new ObjectNotFoundException("Universidade não encontrada."))
                    .when(universityService).deleteUniversity(universityId);

            mockMvc.perform(delete("/api/universities/{universityId}", universityId)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Universidade não encontrada."));
        }
    }
}
