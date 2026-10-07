package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.StateService;
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
@DisplayName("Testes de Integração/Controller - StateController")
class StateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private StateService stateService;

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
        @DisplayName("POST /api/states - Cadastra estado com sucesso com role ADMIN (201 Created)")
        void shouldCreateStateSuccessfullyWhenAdmin() throws Exception {
            StateDTO requestDTO = new StateDTO("Paraíba");
            when(stateService.createState(any(StateDTO.class))).thenReturn(requestDTO);

            mockMvc.perform(post("/api/states")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.stateName").value("Paraíba"));

            verify(stateService).createState(requestDTO);
        }

        @Test
        @DisplayName("GET /api/states - Lista todos os estados com sucesso (200 OK)")
        void shouldGetAllStatesSuccessfullyWhenAuthenticated() throws Exception {
            List<StateDTO> states = List.of(new StateDTO("Paraíba"), new StateDTO("Pernambuco"));
            when(stateService.getAllState()).thenReturn(states);

            mockMvc.perform(get("/api/states")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].stateName").value("Paraíba"))
                    .andExpect(jsonPath("$[1].stateName").value("Pernambuco"));

            verify(stateService).getAllState();
        }

        @Test
        @DisplayName("DELETE /api/states/{stateName} - Exclui estado com sucesso com role ADMIN (204 No Content)")
        void shouldDeleteStateSuccessfullyWhenAdmin() throws Exception {
            String stateName = "Paraíba";
            doNothing().when(stateService).deleteState(stateName);

            mockMvc.perform(delete("/api/states/{stateName}", stateName)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNoContent());

            verify(stateService).deleteState(stateName);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/states - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateStateUnauthenticated() throws Exception {
            StateDTO requestDTO = new StateDTO("Paraíba");

            mockMvc.perform(post("/api/states")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/states - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToCreateState() throws Exception {
            StateDTO requestDTO = new StateDTO("Paraíba");

            mockMvc.perform(post("/api/states")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/states - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllStatesUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/states"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/states/{stateName} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteStateUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/states/Paraíba"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/states/{stateName} - Falha com 403 Forbidden quando usuário não for ADMIN")
        void shouldReturnForbiddenWhenNonAdminAttemptsToDeleteState() throws Exception {
            mockMvc.perform(delete("/api/states/Paraíba")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/states - Falha com 400 Bad Request quando nome do estado for em branco")
        void shouldReturnBadRequestWhenStateNameIsBlank() throws Exception {
            StateDTO requestDTO = new StateDTO("");

            mockMvc.perform(post("/api/states")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/states - Falha com 409 Conflict quando estado já existir")
        void shouldReturnConflictWhenStateAlreadyExists() throws Exception {
            StateDTO requestDTO = new StateDTO("Paraíba");
            when(stateService.createState(any(StateDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("O estado com esse nome já existe."));

            mockMvc.perform(post("/api/states")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("O estado com esse nome já existe."));
        }

        @Test
        @DisplayName("DELETE /api/states/{stateName} - Falha com 404 Not Found quando estado não existir")
        void shouldReturnNotFoundWhenStateNotFoundOnDelete() throws Exception {
            String stateName = "Inexistente";
            doThrow(new ObjectNotFoundException("O estado não existe."))
                    .when(stateService).deleteState(stateName);

            mockMvc.perform(delete("/api/states/{stateName}", stateName)
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("O estado não existe."));
        }
    }
}
