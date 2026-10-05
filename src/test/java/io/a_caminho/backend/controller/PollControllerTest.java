package io.a_caminho.backend.controller;

import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.PollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - PollController")
class PollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PollService pollService;

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build());
    }

    private UserPrincipal createStudentPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("estudante@ufpb.br")
                .role(UserRole.STUDENT)
                .build());
    }

    private UserPrincipal createBusDriverPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("motorista@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build());
    }

    private PollListDTO createSamplePollListDTO(UUID pollId) {
        return new PollListDTO(
                pollId,
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalDate.now(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(5),
                15
        );
    }

    private PollDTO createSamplePollDTO(UUID pollId) {
        return new PollDTO(
                pollId,
                "Rota Universitária Manhã",
                Shift.MORNING,
                LocalDate.now(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(5),
                45,
                15,
                Set.of(new UniversityResponseDTO(UUID.randomUUID(), "UFPB", "Campus I", "João Pessoa", "Paraíba")),
                Set.of(new PollOptionResponseDTO(UUID.randomUUID(), "Praça Central", 5L))
        );
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("GET /api/polls - Lista enquetes da prefeitura com sucesso (200 OK)")
        void shouldGetAllPollsFromMunicipalitySuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            PollListDTO pollListDTO = createSamplePollListDTO(pollId);

            when(pollService.getAllPollFromMunicipality(userId)).thenReturn(List.of(pollListDTO));

            mockMvc.perform(get("/api/polls")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].pollId").value(pollId.toString()))
                    .andExpect(jsonPath("$[0].routeName").value("Rota Universitária Manhã"))
                    .andExpect(jsonPath("$[0].shift").value("Manhã"))
                    .andExpect(jsonPath("$[0].confirmedVotesCount").value(15));

            verify(pollService).getAllPollFromMunicipality(userId);
        }

        @Test
        @DisplayName("GET /api/polls/open - Lista enquetes abertas para o estudante com sucesso (200 OK)")
        void shouldGetOpenPollsToStudentSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            PollListDTO pollListDTO = createSamplePollListDTO(pollId);

            when(pollService.getOpenPollsToStudent(userId)).thenReturn(List.of(pollListDTO));

            mockMvc.perform(get("/api/polls/open")
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].pollId").value(pollId.toString()))
                    .andExpect(jsonPath("$[0].routeName").value("Rota Universitária Manhã"))
                    .andExpect(jsonPath("$[0].shift").value("Manhã"));

            verify(pollService).getOpenPollsToStudent(userId);
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Consulta detalhes da enquete com sucesso como PREFEITURA dona (200 OK)")
        void shouldGetPollDetailsSuccessfullyWhenMunicipalityOwner() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            PollDTO pollDTO = createSamplePollDTO(pollId);

            when(pollService.getPoll(userId, pollId)).thenReturn(pollDTO);

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pollId").value(pollId.toString()))
                    .andExpect(jsonPath("$.routeName").value("Rota Universitária Manhã"))
                    .andExpect(jsonPath("$.shift").value("Manhã"))
                    .andExpect(jsonPath("$.totalCapacity").value(45))
                    .andExpect(jsonPath("$.confirmedVotesCount").value(15));

            verify(pollService).getPoll(userId, pollId);
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Consulta detalhes da enquete com sucesso como ESTUDANTE elegível (200 OK)")
        void shouldGetPollDetailsSuccessfullyWhenStudentEligible() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            PollDTO pollDTO = createSamplePollDTO(pollId);

            when(pollService.getPoll(userId, pollId)).thenReturn(pollDTO);

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pollId").value(pollId.toString()))
                    .andExpect(jsonPath("$.routeName").value("Rota Universitária Manhã"))
                    .andExpect(jsonPath("$.shift").value("Manhã"));

            verify(pollService).getPoll(userId, pollId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("GET /api/polls - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllPollsUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/polls"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/polls - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenGetAllPollsAsStudent() throws Exception {
            mockMvc.perform(get("/api/polls")
                            .with(user(createStudentPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenGetAllPollsAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/polls")
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/open - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetOpenPollsUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/polls/open"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/polls/open - Falha com 403 Forbidden quando usuário for MUNICIPALITY")
        void shouldReturnForbiddenWhenGetOpenPollsAsMunicipality() throws Exception {
            mockMvc.perform(get("/api/polls/open")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/open - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenGetOpenPollsAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/polls/open")
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetPollByIdUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenGetPollByIdAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}", UUID.randomUUID())
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("GET /api/polls - Falha com 404 Not Found quando prefeitura não for encontrada")
        void shouldReturnNotFoundWhenMunicipalityNotFoundOnGetAll() throws Exception {
            UUID userId = UUID.randomUUID();

            when(pollService.getAllPollFromMunicipality(userId))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não encontrada."));

            mockMvc.perform(get("/api/polls")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Prefeitura não encontrada."));
        }

        @Test
        @DisplayName("GET /api/polls/open - Falha com 404 Not Found quando perfil do estudante não for encontrado")
        void shouldReturnNotFoundWhenStudentNotFoundOnGetOpen() throws Exception {
            UUID userId = UUID.randomUUID();

            when(pollService.getOpenPollsToStudent(userId))
                    .thenThrow(new ObjectNotFoundException("Usuário não encontrado."));

            mockMvc.perform(get("/api/polls/open")
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Usuário não encontrado."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 404 Not Found quando enquete não for encontrada")
        void shouldReturnNotFoundWhenPollNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollService.getPoll(userId, pollId))
                    .thenThrow(new ObjectNotFoundException("Enquete não encontrada."));

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Enquete não encontrada."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 403 Forbidden quando prefeitura não for a proprietária da enquete")
        void shouldReturnForbiddenWhenMunicipalityIsNotOwner() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollService.getPoll(userId, pollId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para visualizar esta enquete pois não é o proprietário."));

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para visualizar esta enquete pois não é o proprietário."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 403 Forbidden quando estudante for de outro município (CrossMunicipalityAccessException)")
        void shouldReturnForbiddenWhenStudentFromOtherMunicipality() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollService.getPoll(userId, pollId))
                    .thenThrow(new CrossMunicipalityAccessException("O estudante não pertence ao município responsável por esta enquete."));

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("O estudante não pertence ao município responsável por esta enquete."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId} - Falha com 400 Bad Request quando universidade do estudante não for atendida pela rota (CanNotVoteException)")
        void shouldReturnBadRequestWhenStudentUniversityNotTargeted() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollService.getPoll(userId, pollId))
                    .thenThrow(new CanNotVoteException("O estudante não pertence à universidade atendida por esta enquete."));

            mockMvc.perform(get("/api/polls/{pollId}", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("O estudante não pertence à universidade atendida por esta enquete."));
        }
    }
}
