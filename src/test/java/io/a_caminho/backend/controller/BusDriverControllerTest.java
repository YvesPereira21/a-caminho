package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.user.UserDTO;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.BusDriverService;
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
@DisplayName("Testes de Integração/Controller - BusDriverController")
class BusDriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private BusDriverService busDriverService;

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@cidade.gov.br")
                .role(UserRole.MUNICIPALITY)
                .build());
    }

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
        @DisplayName("POST /api/bus-drivers - Cadastra motorista com sucesso como MUNICIPALITY (201 Created)")
        void shouldCreateBusDriverSuccessfullyWhenMunicipalityRole() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();
            UserDTO userDTO = new UserDTO("motorista@cidade.gov.br", "senha1234");
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Sebastião da Silva", userDTO);
            BusDriverResponseDTO responseDTO = new BusDriverResponseDTO(busDriverId, "Sebastião da Silva", "motorista@cidade.gov.br");

            when(busDriverService.createBusDriverAccount(any(BusDriverRequestDTO.class), eq(municipalityUserId)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createMunicipalityPrincipal(municipalityUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.busDriverId").value(busDriverId.toString()))
                    .andExpect(jsonPath("$.busDriverName").value("Sebastião da Silva"))
                    .andExpect(jsonPath("$.email").value("motorista@cidade.gov.br"));

            verify(busDriverService).createBusDriverAccount(requestDTO, municipalityUserId);
        }

        @Test
        @DisplayName("GET /api/bus-drivers/{busDriverId} - Busca motorista por ID com sucesso como MUNICIPALITY (200 OK)")
        void shouldGetBusDriverByIdSuccessfullyWhenMunicipalityRole() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();
            BusDriverResponseDTO responseDTO = new BusDriverResponseDTO(busDriverId, "Sebastião da Silva", "motorista@cidade.gov.br");

            when(busDriverService.getBusDriverById(busDriverId, municipalityUserId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/bus-drivers/{busDriverId}", busDriverId)
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.busDriverId").value(busDriverId.toString()))
                    .andExpect(jsonPath("$.busDriverName").value("Sebastião da Silva"));

            verify(busDriverService).getBusDriverById(busDriverId, municipalityUserId);
        }

        @Test
        @DisplayName("GET /api/bus-drivers - Lista motoristas da prefeitura autenticada com sucesso (200 OK)")
        void shouldGetAllBusDriversFromMunicipalitySuccessfullyWhenMunicipalityRole() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();
            BusDriverResponseDTO responseDTO = new BusDriverResponseDTO(busDriverId, "Sebastião da Silva", "motorista@cidade.gov.br");

            when(busDriverService.getAllBusDriversFromMunicipality(municipalityUserId))
                    .thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/bus-drivers")
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].busDriverId").value(busDriverId.toString()))
                    .andExpect(jsonPath("$[0].busDriverName").value("Sebastião da Silva"));

            verify(busDriverService).getAllBusDriversFromMunicipality(municipalityUserId);
        }

        @Test
        @DisplayName("DELETE /api/bus-drivers/{busDriverId} - Exclui motorista com sucesso como MUNICIPALITY (204 No Content)")
        void shouldDeleteBusDriverAccountSuccessfullyWhenMunicipalityRole() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();

            doNothing().when(busDriverService).deleteBusDriverAccount(busDriverId, municipalityUserId);

            mockMvc.perform(delete("/api/bus-drivers/{busDriverId}", busDriverId)
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isNoContent());

            verify(busDriverService).deleteBusDriverAccount(busDriverId, municipalityUserId);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateBusDriverUnauthenticated() throws Exception {
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Motorista", new UserDTO("motorista@cidade.gov.br", "senha1234"));

            mockMvc.perform(post("/api/bus-drivers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 403 Forbidden quando usuário for ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToCreateBusDriver() throws Exception {
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Motorista", new UserDTO("motorista@cidade.gov.br", "senha1234"));

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createAdminPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenStudentAttemptsToCreateBusDriver() throws Exception {
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Motorista", new UserDTO("motorista@cidade.gov.br", "senha1234"));

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 400 Bad Request quando payload for inválido")
        void shouldReturnBadRequestWhenCreateBusDriverPayloadInvalid() throws Exception {
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("", null);

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 404 Not Found quando prefeitura não for encontrada")
        void shouldReturnNotFoundWhenMunicipalityNotFoundOnCreate() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Motorista", new UserDTO("motorista@cidade.gov.br", "senha1234"));
            when(busDriverService.createBusDriverAccount(any(BusDriverRequestDTO.class), eq(municipalityUserId)))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não existe."));

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createMunicipalityPrincipal(municipalityUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Prefeitura não existe."));
        }

        @Test
        @DisplayName("POST /api/bus-drivers - Falha com 409 Conflict quando email já estiver cadastrado")
        void shouldReturnConflictWhenEmailAlreadyExistsOnCreate() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            BusDriverRequestDTO requestDTO = new BusDriverRequestDTO("Motorista", new UserDTO("motorista@cidade.gov.br", "senha1234"));
            when(busDriverService.createBusDriverAccount(any(BusDriverRequestDTO.class), eq(municipalityUserId)))
                    .thenThrow(new ObjectAlreadyExistsException("Usuário já cadastrado com o e-mail: motorista@cidade.gov.br"));

            mockMvc.perform(post("/api/bus-drivers")
                            .with(user(createMunicipalityPrincipal(municipalityUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Usuário já cadastrado com o e-mail: motorista@cidade.gov.br"));
        }

        @Test
        @DisplayName("GET /api/bus-drivers/{busDriverId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetBusDriverByIdUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/bus-drivers/{busDriverId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/bus-drivers/{busDriverId} - Falha com 403 Forbidden quando usuário for ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToGetBusDriver() throws Exception {
            mockMvc.perform(get("/api/bus-drivers/{busDriverId}", UUID.randomUUID())
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/bus-drivers/{busDriverId} - Falha com 403 Forbidden quando motorista pertencer a outra prefeitura")
        void shouldReturnForbiddenWhenCrossMunicipalityAccessOnGet() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();

            when(busDriverService.getBusDriverById(busDriverId, municipalityUserId))
                    .thenThrow(new CrossMunicipalityAccessException("Motorista não encontrado."));

            mockMvc.perform(get("/api/bus-drivers/{busDriverId}", busDriverId)
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Motorista não encontrado."));
        }

        @Test
        @DisplayName("GET /api/bus-drivers - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetAllBusDriversUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/bus-drivers"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/bus-drivers - Falha com 403 Forbidden quando usuário for ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToGetAllBusDrivers() throws Exception {
            mockMvc.perform(get("/api/bus-drivers")
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/bus-drivers/{busDriverId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteBusDriverUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/bus-drivers/{busDriverId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/bus-drivers/{busDriverId} - Falha com 403 Forbidden quando usuário for ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToDeleteBusDriver() throws Exception {
            mockMvc.perform(delete("/api/bus-drivers/{busDriverId}", UUID.randomUUID())
                            .with(user(createAdminPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/bus-drivers/{busDriverId} - Falha com 403 Forbidden quando motorista pertencer a outra prefeitura")
        void shouldReturnForbiddenWhenCrossMunicipalityAccessOnDelete() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID busDriverId = UUID.randomUUID();

            doThrow(new CrossMunicipalityAccessException("Motorista não encontrado."))
                    .when(busDriverService).deleteBusDriverAccount(busDriverId, municipalityUserId);

            mockMvc.perform(delete("/api/bus-drivers/{busDriverId}", busDriverId)
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Motorista não encontrado."));
        }
    }
}
