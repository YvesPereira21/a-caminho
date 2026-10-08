package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.pollvote.VoteRequestDTO;
import io.a_caminho.backend.dto.pollvote.VoteResponseDTO;
import io.a_caminho.backend.exception.CanNotVoteException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.PollVoteService;
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

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - PollVoteController")
class PollVoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PollVoteService pollVoteService;

    private UserPrincipal createStudentPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("aluno@ufpb.br")
                .role(UserRole.STUDENT)
                .build());
    }

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@municipio.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build());
    }

    private UserPrincipal createBusDriverPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("motorista@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build());
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Registra voto com sucesso como STUDENT (201 Created)")
        void shouldVoteSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            doNothing().when(pollVoteService).vote(eq(userId), eq(pollId), any(VoteRequestDTO.class));

            mockMvc.perform(post("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            verify(pollVoteService).vote(eq(userId), eq(pollId), any(VoteRequestDTO.class));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/my-vote - Consulta próprio voto com sucesso como STUDENT (200 OK)")
        void shouldGetMyVoteSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            VoteResponseDTO responseDTO = new VoteResponseDTO(
                    UUID.randomUUID(), pollId, optionId, "Praça Central", true, LocalDateTime.now()
            );

            when(pollVoteService.getMyVote(userId, pollId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/polls/{pollId}/my-vote", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pollId").value(pollId.toString()))
                    .andExpect(jsonPath("$.optionId").value(optionId.toString()))
                    .andExpect(jsonPath("$.stopName").value("Praça Central"))
                    .andExpect(jsonPath("$.returnConfirmed").value(true));

            verify(pollVoteService).getMyVote(userId, pollId);
        }

        @Test
        @DisplayName("DELETE /api/polls/{pollId}/votes - Cancela voto com sucesso como STUDENT (204 No Content)")
        void shouldCancelVoteSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            doNothing().when(pollVoteService).cancelVote(userId, pollId);

            mockMvc.perform(delete("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isNoContent());

            verify(pollVoteService).cancelVote(userId, pollId);
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Lista passageiros confirmados com sucesso como MUNICIPALITY (200 OK)")
        void shouldGetPollPassengersSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            PollPassengerDTO passengerDTO = new PollPassengerDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Ana Silva",
                    "202300123",
                    "Engenharia de Software",
                    UUID.randomUUID(),
                    "Praça Central",
                    true,
                    LocalDateTime.now()
            );

            when(pollVoteService.getPollPassengers(userId, pollId)).thenReturn(List.of(passengerDTO));

            mockMvc.perform(get("/api/polls/{pollId}/passengers", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].studentName").value("Ana Silva"))
                    .andExpect(jsonPath("$[0].registrationNumber").value("202300123"))
                    .andExpect(jsonPath("$[0].courseName").value("Engenharia de Software"))
                    .andExpect(jsonPath("$[0].stopName").value("Praça Central"))
                    .andExpect(jsonPath("$[0].returnConfirmed").value(true));

            verify(pollVoteService).getPollPassengers(userId, pollId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenVoteUnauthenticated() throws Exception {
            VoteRequestDTO requestDTO = new VoteRequestDTO(UUID.randomUUID(), true);

            mockMvc.perform(post("/api/polls/{pollId}/votes", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 403 Forbidden quando usuário for MUNICIPALITY")
        void shouldReturnForbiddenWhenVoteAsMunicipality() throws Exception {
            VoteRequestDTO requestDTO = new VoteRequestDTO(UUID.randomUUID(), true);

            mockMvc.perform(post("/api/polls/{pollId}/votes", UUID.randomUUID())
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenVoteAsBusDriver() throws Exception {
            VoteRequestDTO requestDTO = new VoteRequestDTO(UUID.randomUUID(), true);

            mockMvc.perform(post("/api/polls/{pollId}/votes", UUID.randomUUID())
                            .with(user(createBusDriverPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/my-vote - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetMyVoteUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}/my-vote", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/my-vote - Falha com 403 Forbidden quando usuário for MUNICIPALITY")
        void shouldReturnForbiddenWhenGetMyVoteAsMunicipality() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}/my-vote", UUID.randomUUID())
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/polls/{pollId}/votes - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCancelVoteUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/polls/{pollId}/votes", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/polls/{pollId}/votes - Falha com 403 Forbidden quando usuário for MUNICIPALITY")
        void shouldReturnForbiddenWhenCancelVoteAsMunicipality() throws Exception {
            mockMvc.perform(delete("/api/polls/{pollId}/votes", UUID.randomUUID())
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetPassengersUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}/passengers", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenGetPassengersAsStudent() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}/passengers", UUID.randomUUID())
                            .with(user(createStudentPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenGetPassengersAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/polls/{pollId}/passengers", UUID.randomUUID())
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 400 Bad Request quando optionId for nulo (@Valid)")
        void shouldReturnBadRequestWhenOptionIdIsNull() throws Exception {
            VoteRequestDTO invalidDTO = new VoteRequestDTO(null, true);

            mockMvc.perform(post("/api/polls/{pollId}/votes", UUID.randomUUID())
                            .with(user(createStudentPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 400 Bad Request quando enquete estiver encerrada ou estudante já votou (CanNotVoteException)")
        void shouldReturnBadRequestWhenCanNotVote() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            doThrow(new CanNotVoteException("A enquete não está mais aberta para votar."))
                    .when(pollVoteService).vote(eq(userId), eq(pollId), any(VoteRequestDTO.class));

            mockMvc.perform(post("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("A enquete não está mais aberta para votar."));
        }

        @Test
        @DisplayName("POST /api/polls/{pollId}/votes - Falha com 404 Not Found quando enquete ou ponto não existir")
        void shouldReturnNotFoundWhenEntityNotFoundOnVote() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            VoteRequestDTO requestDTO = new VoteRequestDTO(optionId, true);

            doThrow(new ObjectNotFoundException("Enquete não encontrada."))
                    .when(pollVoteService).vote(eq(userId), eq(pollId), any(VoteRequestDTO.class));

            mockMvc.perform(post("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Enquete não encontrada."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/my-vote - Falha com 404 Not Found quando voto não for encontrado")
        void shouldReturnNotFoundWhenMyVoteNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollVoteService.getMyVote(userId, pollId))
                    .thenThrow(new ObjectNotFoundException("Você ainda não votou nesta enquete."));

            mockMvc.perform(get("/api/polls/{pollId}/my-vote", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Você ainda não votou nesta enquete."));
        }

        @Test
        @DisplayName("DELETE /api/polls/{pollId}/votes - Falha com 400 Bad Request quando enquete estiver encerrada")
        void shouldReturnBadRequestWhenCancelVoteAfterPollClosed() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            doThrow(new CanNotVoteException("Não é mais possível cancelar o voto após o encerramento da enquete."))
                    .when(pollVoteService).cancelVote(userId, pollId);

            mockMvc.perform(delete("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("Não é mais possível cancelar o voto após o encerramento da enquete."));
        }

        @Test
        @DisplayName("DELETE /api/polls/{pollId}/votes - Falha com 404 Not Found quando voto não for encontrado para cancelamento")
        void shouldReturnNotFoundWhenVoteNotFoundForCancellation() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            doThrow(new ObjectNotFoundException("Voto não encontrado para cancelamento."))
                    .when(pollVoteService).cancelVote(userId, pollId);

            mockMvc.perform(delete("/api/polls/{pollId}/votes", pollId)
                            .with(user(createStudentPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Voto não encontrado para cancelamento."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Falha com 404 Not Found quando enquete não for encontrada")
        void shouldReturnNotFoundWhenPollNotFoundOnGetPassengers() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollVoteService.getPollPassengers(userId, pollId))
                    .thenThrow(new ObjectNotFoundException("Enquete não encontrada."));

            mockMvc.perform(get("/api/polls/{pollId}/passengers", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Enquete não encontrada."));
        }

        @Test
        @DisplayName("GET /api/polls/{pollId}/passengers - Falha com 403 Forbidden quando prefeitura não for proprietária")
        void shouldReturnForbiddenWhenMunicipalityIsNotOwnerOnGetPassengers() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID pollId = UUID.randomUUID();

            when(pollVoteService.getPollPassengers(userId, pollId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para isso."));

            mockMvc.perform(get("/api/polls/{pollId}/passengers", pollId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para isso."));
        }
    }
}
