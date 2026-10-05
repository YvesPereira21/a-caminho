package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.PollOptionService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - PollOptionController")
class PollOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PollOptionService pollOptionService;

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build());
    }

    private UserPrincipal createStudentPrincipal() {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email("estudante@ufpb.br")
                .role(UserRole.STUDENT)
                .build());
    }

    private UserPrincipal createBusDriverPrincipal() {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email("motorista@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build());
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/poll-options - Cadastra ponto de ônibus com sucesso (201 Created)")
        void shouldCreatePollOptionSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Praça Central");
            PollOptionResponseDTO responseDTO = new PollOptionResponseDTO(optionId, "Praça Central", 0L);

            when(pollOptionService.createPollOption(eq(userId), any(PollOptionDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.optionId").value(optionId.toString()))
                    .andExpect(jsonPath("$.stopName").value("Praça Central"))
                    .andExpect(jsonPath("$.voteCount").value(0));

            verify(pollOptionService).createPollOption(eq(userId), any(PollOptionDTO.class));
        }

        @Test
        @DisplayName("GET /api/poll-options - Lista todos os pontos da prefeitura com sucesso (200 OK)")
        void shouldGetAllPollOptionsSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionResponseDTO responseDTO = new PollOptionResponseDTO(optionId, "Praça Central", 0L);

            when(pollOptionService.getAllPollOptions(userId))
                    .thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/poll-options")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].optionId").value(optionId.toString()))
                    .andExpect(jsonPath("$[0].stopName").value("Praça Central"));

            verify(pollOptionService).getAllPollOptions(userId);
        }

        @Test
        @DisplayName("GET /api/poll-options/{optionId} - Busca ponto por ID com sucesso (200 OK)")
        void shouldGetPollOptionByIdSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionResponseDTO responseDTO = new PollOptionResponseDTO(optionId, "Praça Central", 0L);

            when(pollOptionService.getPollOption(userId, optionId))
                    .thenReturn(responseDTO);

            mockMvc.perform(get("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.optionId").value(optionId.toString()))
                    .andExpect(jsonPath("$.stopName").value("Praça Central"));

            verify(pollOptionService).getPollOption(userId, optionId);
        }

        @Test
        @DisplayName("PUT /api/poll-options/{optionId} - Atualiza ponto com sucesso (204 No Content)")
        void shouldUpdatePollOptionSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Ponto Atualizado");

            doNothing().when(pollOptionService).updatePollOption(eq(userId), eq(optionId), any(PollOptionDTO.class));

            mockMvc.perform(put("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNoContent());

            verify(pollOptionService).updatePollOption(eq(userId), eq(optionId), any(PollOptionDTO.class));
        }

        @Test
        @DisplayName("DELETE /api/poll-options/{optionId} - Remove ponto com sucesso (204 No Content)")
        void shouldDeletePollOptionSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            doNothing().when(pollOptionService).deleteOption(userId, optionId);

            mockMvc.perform(delete("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNoContent());

            verify(pollOptionService).deleteOption(userId, optionId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/poll-options - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateUnauthenticated() throws Exception {
            PollOptionDTO requestDTO = new PollOptionDTO("Ponto");

            mockMvc.perform(post("/api/poll-options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/poll-options - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenCreateAsStudent() throws Exception {
            PollOptionDTO requestDTO = new PollOptionDTO("Ponto");

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/poll-options - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenCreateAsBusDriver() throws Exception {
            PollOptionDTO requestDTO = new PollOptionDTO("Ponto");

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createBusDriverPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/poll-options - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenListUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/poll-options"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/poll-options - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenListAsStudent() throws Exception {
            mockMvc.perform(get("/api/poll-options")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/poll-options/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenGetByIdAsStudent() throws Exception {
            mockMvc.perform(get("/api/poll-options/{optionId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/poll-options/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenUpdateAsStudent() throws Exception {
            PollOptionDTO requestDTO = new PollOptionDTO("Ponto");

            mockMvc.perform(put("/api/poll-options/{optionId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/poll-options/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenDeleteAsStudent() throws Exception {
            mockMvc.perform(delete("/api/poll-options/{optionId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/poll-options - Falha com 400 Bad Request quando nome do ponto for em branco (@Valid)")
        void shouldReturnBadRequestWhenStopNameIsBlankOnCreate() throws Exception {
            PollOptionDTO invalidDTO = new PollOptionDTO("");

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/poll-options - Falha com 404 Not Found quando prefeitura não for encontrada")
        void shouldReturnNotFoundWhenMunicipalityNotFoundOnCreate() throws Exception {
            UUID userId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Praça Central");

            when(pollOptionService.createPollOption(eq(userId), any(PollOptionDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não existe."));

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Prefeitura não existe."));
        }

        @Test
        @DisplayName("POST /api/poll-options - Falha com 409 Conflict quando ponto com mesmo nome já existir")
        void shouldReturnConflictWhenStopNameAlreadyExistsOnCreate() throws Exception {
            UUID userId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Praça Central");

            when(pollOptionService.createPollOption(eq(userId), any(PollOptionDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Já existe um ponto de ônibus cadastrado com este nome."));

            mockMvc.perform(post("/api/poll-options")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0]").value("Já existe um ponto de ônibus cadastrado com este nome."));
        }

        @Test
        @DisplayName("GET /api/poll-options/{id} - Falha com 404 Not Found quando ponto não for encontrado")
        void shouldReturnNotFoundWhenGetNonExistentOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            when(pollOptionService.getPollOption(userId, optionId))
                    .thenThrow(new ObjectNotFoundException("Ponto não cadastrado."));

            mockMvc.perform(get("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Ponto não cadastrado."));
        }

        @Test
        @DisplayName("GET /api/poll-options/{id} - Falha com 403 Forbidden quando ponto for de outra prefeitura")
        void shouldReturnForbiddenWhenGetOtherMunicipalityOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            when(pollOptionService.getPollOption(userId, optionId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para isso."));

            mockMvc.perform(get("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }

        @Test
        @DisplayName("PUT /api/poll-options/{id} - Falha com 400 Bad Request quando nome do ponto for em branco (@Valid)")
        void shouldReturnBadRequestWhenStopNameIsBlankOnUpdate() throws Exception {
            PollOptionDTO invalidDTO = new PollOptionDTO("");

            mockMvc.perform(put("/api/poll-options/{optionId}", UUID.randomUUID())
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("PUT /api/poll-options/{id} - Falha com 404 Not Found quando ponto não for encontrado")
        void shouldReturnNotFoundWhenUpdateNonExistentOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Novo Nome");

            doThrow(new ObjectNotFoundException("Ponto não cadastrado."))
                    .when(pollOptionService).updatePollOption(eq(userId), eq(optionId), any(PollOptionDTO.class));

            mockMvc.perform(put("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Ponto não cadastrado."));
        }

        @Test
        @DisplayName("PUT /api/poll-options/{id} - Falha com 403 Forbidden quando ponto for de outra prefeitura")
        void shouldReturnForbiddenWhenUpdateOtherMunicipalityOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Novo Nome");

            doThrow(new UserIsNotOwnerException("Você não tem permissão para isso."))
                    .when(pollOptionService).updatePollOption(eq(userId), eq(optionId), any(PollOptionDTO.class));

            mockMvc.perform(put("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }

        @Test
        @DisplayName("PUT /api/poll-options/{id} - Falha com 409 Conflict quando novo nome já existir no município")
        void shouldReturnConflictWhenNewNameAlreadyExistsOnUpdate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            PollOptionDTO requestDTO = new PollOptionDTO("Nome Repetido");

            doThrow(new ObjectAlreadyExistsException("Já existe um ponto de ônibus cadastrado com este nome."))
                    .when(pollOptionService).updatePollOption(eq(userId), eq(optionId), any(PollOptionDTO.class));

            mockMvc.perform(put("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0]").value("Já existe um ponto de ônibus cadastrado com este nome."));
        }

        @Test
        @DisplayName("DELETE /api/poll-options/{id} - Falha com 404 Not Found quando ponto não for encontrado")
        void shouldReturnNotFoundWhenDeleteNonExistentOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            doThrow(new ObjectNotFoundException("Ponto não cadastrado."))
                    .when(pollOptionService).deleteOption(userId, optionId);

            mockMvc.perform(delete("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Ponto não cadastrado."));
        }

        @Test
        @DisplayName("DELETE /api/poll-options/{id} - Falha com 403 Forbidden quando ponto for de outra prefeitura")
        void shouldReturnForbiddenWhenDeleteOtherMunicipalityOption() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            doThrow(new UserIsNotOwnerException("Você não tem permissão para isso."))
                    .when(pollOptionService).deleteOption(userId, optionId);

            mockMvc.perform(delete("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }

        @Test
        @DisplayName("DELETE /api/poll-options/{id} - Falha com 400 Bad Request quando existirem votos vinculados ao ponto")
        void shouldReturnBadRequestWhenDeleteOptionWithLinkedVotes() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();

            doThrow(new IllegalArgumentException("Não é possível excluir este ponto pois já existem votos vinculados a ele."))
                    .when(pollOptionService).deleteOption(userId, optionId);

            mockMvc.perform(delete("/api/poll-options/{optionId}", optionId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("Não é possível excluir este ponto pois já existem votos vinculados a ele."));
        }
    }
}
