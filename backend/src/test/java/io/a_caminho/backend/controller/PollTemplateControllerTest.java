package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.InvalidTimeException;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.PollTemplateService;
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

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - PollTemplateController")
class PollTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PollTemplateService pollTemplateService;

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
                .email("estudante@universidade.edu.br")
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

    private PollTemplateDTO createSampleTemplateDTO(UUID templateId) {
        return new PollTemplateDTO(
                templateId,
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalTime.of(6, 0),
                LocalTime.of(12, 0),
                true,
                Set.of(new UniversityResponseDTO(UUID.randomUUID(), "UFPB", "Campus I", "João Pessoa", "Paraíba")),
                Set.of(new PollOptionResponseDTO(UUID.randomUUID(), "Praça Central", 0L))
        );
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/poll-templates - Cria modelo com sucesso como MUNICIPALITY (201 Created)")
        void shouldCreatePollTemplateSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota Universitária Manhã",
                    Shift.MORNING,
                    LocalTime.of(6, 0),
                    LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()),
                    Set.of(UUID.randomUUID())
            );
            PollTemplateDTO responseDTO = createSampleTemplateDTO(templateId);

            when(pollTemplateService.createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.templateId").value(templateId.toString()))
                    .andExpect(jsonPath("$.routeName").value("Rota Universitária Manhã"))
                    .andExpect(jsonPath("$.shift").value("Manhã"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(pollTemplateService).createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class));
        }

        @Test
        @DisplayName("GET /api/poll-templates - Lista todos os modelos da prefeitura com sucesso (200 OK)")
        void shouldGetAllPollTemplatesSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();
            PollTemplateDTO templateDTO = createSampleTemplateDTO(templateId);

            when(pollTemplateService.getAllPollTemplateFromMunicipality(userId))
                    .thenReturn(List.of(templateDTO));

            mockMvc.perform(get("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].templateId").value(templateId.toString()))
                    .andExpect(jsonPath("$[0].routeName").value("Rota Universitária Manhã"));

            verify(pollTemplateService).getAllPollTemplateFromMunicipality(userId);
        }

        @Test
        @DisplayName("GET /api/poll-templates/{templateId} - Busca modelo por ID com sucesso (200 OK)")
        void shouldGetPollTemplateByIdSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();
            PollTemplateDTO templateDTO = createSampleTemplateDTO(templateId);

            when(pollTemplateService.getPollTemplate(userId, templateId))
                    .thenReturn(templateDTO);

            mockMvc.perform(get("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.templateId").value(templateId.toString()))
                    .andExpect(jsonPath("$.routeName").value("Rota Universitária Manhã"));

            verify(pollTemplateService).getPollTemplate(userId, templateId);
        }

        @Test
        @DisplayName("PATCH /api/poll-templates/{templateId}/deactivate - Desativa modelo com sucesso (204 No Content)")
        void shouldDeactivatePollTemplateSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doNothing().when(pollTemplateService).deactivatePollTemplate(userId, templateId);

            mockMvc.perform(patch("/api/poll-templates/{templateId}/deactivate", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNoContent());

            verify(pollTemplateService).deactivatePollTemplate(userId, templateId);
        }

        @Test
        @DisplayName("DELETE /api/poll-templates/{templateId} - Remove modelo com sucesso (204 No Content)")
        void shouldDeletePollTemplateSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doNothing().when(pollTemplateService).deletePollTemplate(userId, templateId);

            mockMvc.perform(delete("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNoContent());

            verify(pollTemplateService).deletePollTemplate(userId, templateId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateUnauthenticated() throws Exception {
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            mockMvc.perform(post("/api/poll-templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenCreateAsStudent() throws Exception {
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenCreateAsBusDriver() throws Exception {
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createBusDriverPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/poll-templates - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenListUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/poll-templates"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/poll-templates - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenListAsStudent() throws Exception {
            mockMvc.perform(get("/api/poll-templates")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/poll-templates/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenGetByIdAsStudent() throws Exception {
            mockMvc.perform(get("/api/poll-templates/{templateId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/poll-templates/{id}/deactivate - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenDeactivateAsStudent() throws Exception {
            mockMvc.perform(patch("/api/poll-templates/{templateId}/deactivate", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/poll-templates/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenDeleteAsStudent() throws Exception {
            mockMvc.perform(delete("/api/poll-templates/{templateId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 400 Bad Request quando payload for inválido (@Valid)")
        void shouldReturnBadRequestWhenPayloadInvalid() throws Exception {
            PollTemplateCreateDTO invalidDTO = new PollTemplateCreateDTO(
                    "",
                    null,
                    null,
                    null,
                    Collections.emptySet(),
                    Collections.emptySet()
            );

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 400 Bad Request quando horários forem iguais (InvalidTimeException)")
        void shouldReturnBadRequestWhenInvalidTimeExceptionThrown() throws Exception {
            UUID userId = UUID.randomUUID();
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(8, 0), LocalTime.of(8, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            when(pollTemplateService.createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class)))
                    .thenThrow(new InvalidTimeException("Horário de início e término não podem ser iguais."));

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("Horário de início e término não podem ser iguais."));
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 409 Conflict quando modelo já existe (ObjectAlreadyExistsException)")
        void shouldReturnConflictWhenAlreadyExists() throws Exception {
            UUID userId = UUID.randomUUID();
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota Repetida", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            when(pollTemplateService.createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Esse modelo de enquete já existe."));

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0]").value("Esse modelo de enquete já existe."));
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 404 Not Found quando universidade não existir (ObjectNotFoundException)")
        void shouldReturnNotFoundWhenUniversityNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            when(pollTemplateService.createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Uma ou mais universidades informadas não foram encontradas."));

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Uma ou mais universidades informadas não foram encontradas."));
        }

        @Test
        @DisplayName("POST /api/poll-templates - Falha com 403 Forbidden quando ponto for de outro município (UserIsNotOwnerException)")
        void shouldReturnForbiddenWhenOptionBelongsToAnotherMunicipality() throws Exception {
            UUID userId = UUID.randomUUID();
            PollTemplateCreateDTO createDTO = new PollTemplateCreateDTO(
                    "Rota", Shift.MORNING, LocalTime.of(6, 0), LocalTime.of(12, 0),
                    Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID())
            );

            when(pollTemplateService.createPollTemplate(eq(userId), any(PollTemplateCreateDTO.class)))
                    .thenThrow(new UserIsNotOwnerException("Um ou mais pontos não pertencem à sua prefeitura."));

            mockMvc.perform(post("/api/poll-templates")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Um ou mais pontos não pertencem à sua prefeitura."));
        }

        @Test
        @DisplayName("GET /api/poll-templates/{id} - Falha com 404 Not Found quando modelo não existir")
        void shouldReturnNotFoundWhenGetNonExistentTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            when(pollTemplateService.getPollTemplate(userId, templateId))
                    .thenThrow(new ObjectNotFoundException("Modelo de enquete não cadastrado."));

            mockMvc.perform(get("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Modelo de enquete não cadastrado."));
        }

        @Test
        @DisplayName("GET /api/poll-templates/{id} - Falha com 403 Forbidden quando modelo for de outra prefeitura")
        void shouldReturnForbiddenWhenGetOtherMunicipalityTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            when(pollTemplateService.getPollTemplate(userId, templateId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para isso."));

            mockMvc.perform(get("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }

        @Test
        @DisplayName("PATCH /api/poll-templates/{id}/deactivate - Falha com 404 Not Found quando modelo não existir")
        void shouldReturnNotFoundWhenDeactivateNonExistentTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doThrow(new ObjectNotFoundException("Modelo de enquete não cadastrado."))
                    .when(pollTemplateService).deactivatePollTemplate(userId, templateId);

            mockMvc.perform(patch("/api/poll-templates/{templateId}/deactivate", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PATCH /api/poll-templates/{id}/deactivate - Falha com 403 Forbidden quando modelo for de outra prefeitura")
        void shouldReturnForbiddenWhenDeactivateOtherMunicipalityTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doThrow(new UserIsNotOwnerException("Você não tem permissão para isso."))
                    .when(pollTemplateService).deactivatePollTemplate(userId, templateId);

            mockMvc.perform(patch("/api/poll-templates/{templateId}/deactivate", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }

        @Test
        @DisplayName("DELETE /api/poll-templates/{id} - Falha com 404 Not Found quando modelo não existir")
        void shouldReturnNotFoundWhenDeleteNonExistentTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doThrow(new ObjectNotFoundException("Modelo de enquete não cadastrado."))
                    .when(pollTemplateService).deletePollTemplate(userId, templateId);

            mockMvc.perform(delete("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/poll-templates/{id} - Falha com 403 Forbidden quando modelo for de outra prefeitura")
        void shouldReturnForbiddenWhenDeleteOtherMunicipalityTemplate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID templateId = UUID.randomUUID();

            doThrow(new UserIsNotOwnerException("Você não tem permissão para isso."))
                    .when(pollTemplateService).deletePollTemplate(userId, templateId);

            mockMvc.perform(delete("/api/poll-templates/{templateId}", templateId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }
    }
}
