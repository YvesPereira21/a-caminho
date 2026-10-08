package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.BusService;
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
@DisplayName("Testes de Integração/Controller - BusController")
class BusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private BusService busService;

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

    private UserPrincipal createStudentPrincipal() {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email("estudante@ufpb.br")
                .role(UserRole.STUDENT)
                .build());
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/buses - Cadastra ônibus com sucesso (201 Created)")
        void shouldCreateBusSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);
            BusDTO responseDTO = new BusDTO(busId, "Ônibus 01", 44);

            when(busService.createBus(eq(userId), any(BusCreateDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/buses")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.busId").value(busId.toString()))
                    .andExpect(jsonPath("$.busName").value("Ônibus 01"))
                    .andExpect(jsonPath("$.seatsQuantity").value(44));

            verify(busService).createBus(eq(userId), any(BusCreateDTO.class));
        }

        @Test
        @DisplayName("GET /api/buses/{busId} - Busca ônibus por ID com sucesso (200 OK)")
        void shouldGetBusByIdSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusDTO responseDTO = new BusDTO(busId, "Ônibus 01", 44);

            when(busService.getBusById(userId, busId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.busId").value(busId.toString()))
                    .andExpect(jsonPath("$.busName").value("Ônibus 01"))
                    .andExpect(jsonPath("$.seatsQuantity").value(44));

            verify(busService).getBusById(userId, busId);
        }

        @Test
        @DisplayName("GET /api/buses - Lista todos os ônibus da prefeitura com sucesso (200 OK)")
        void shouldGetAllBusesByMunicipalitySuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusDTO responseDTO = new BusDTO(busId, "Ônibus 01", 44);

            when(busService.getAllBusesByMunicipality(userId)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/buses")
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].busId").value(busId.toString()))
                    .andExpect(jsonPath("$[0].busName").value("Ônibus 01"))
                    .andExpect(jsonPath("$[0].seatsQuantity").value(44));

            verify(busService).getAllBusesByMunicipality(userId);
        }

        @Test
        @DisplayName("GET /api/buses/driver - Lista ônibus para motorista com sucesso (200 OK)")
        void shouldGetAllBusesForDriverSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusDTO responseDTO = new BusDTO(busId, "Ônibus 01", 44);

            when(busService.getAllBusesForDriver(userId)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/buses/driver")
                            .with(user(createBusDriverPrincipal(userId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].busId").value(busId.toString()))
                    .andExpect(jsonPath("$[0].busName").value("Ônibus 01"))
                    .andExpect(jsonPath("$[0].seatsQuantity").value(44));

            verify(busService).getAllBusesForDriver(userId);
        }

        @Test
        @DisplayName("PUT /api/buses/{busId} - Atualiza ônibus com sucesso (200 OK)")
        void shouldUpdateBusSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusUpdateDTO requestDTO = new BusUpdateDTO("Ônibus 01 Atualizado", 50);
            BusDTO responseDTO = new BusDTO(busId, "Ônibus 01 Atualizado", 50);

            when(busService.updateBus(eq(userId), eq(busId), any(BusUpdateDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(put("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.busId").value(busId.toString()))
                    .andExpect(jsonPath("$.busName").value("Ônibus 01 Atualizado"))
                    .andExpect(jsonPath("$.seatsQuantity").value(50));

            verify(busService).updateBus(eq(userId), eq(busId), any(BusUpdateDTO.class));
        }

        @Test
        @DisplayName("DELETE /api/buses/{busId} - Exclui ônibus com sucesso (204 No Content)")
        void shouldDeleteBusSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();

            doNothing().when(busService).deleteBus(userId, busId);

            mockMvc.perform(delete("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNoContent());

            verify(busService).deleteBus(userId, busId);
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        @Test
        @DisplayName("POST /api/buses - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenCreateUnauthenticated() throws Exception {
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);

            mockMvc.perform(post("/api/buses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/buses - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenCreateAsStudent() throws Exception {
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);

            mockMvc.perform(post("/api/buses")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/buses - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenCreateAsBusDriver() throws Exception {
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);

            mockMvc.perform(post("/api/buses")
                            .with(user(createBusDriverPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenListUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/buses"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/buses - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenListAsStudent() throws Exception {
            mockMvc.perform(get("/api/buses")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenListAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/buses")
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenGetByIdAsStudent() throws Exception {
            mockMvc.perform(get("/api/buses/{busId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses/{id} - Falha com 403 Forbidden quando usuário for BUS_DRIVER")
        void shouldReturnForbiddenWhenGetByIdAsBusDriver() throws Exception {
            mockMvc.perform(get("/api/buses/{busId}", UUID.randomUUID())
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses/driver - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDriverListUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/buses/driver"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/buses/driver - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenDriverListAsStudent() throws Exception {
            mockMvc.perform(get("/api/buses/driver")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses/driver - Falha com 403 Forbidden quando usuário for MUNICIPALITY")
        void shouldReturnForbiddenWhenDriverListAsMunicipality() throws Exception {
            mockMvc.perform(get("/api/buses/driver")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/buses/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenUpdateAsStudent() throws Exception {
            BusUpdateDTO requestDTO = new BusUpdateDTO("Ônibus 01", 44);

            mockMvc.perform(put("/api/buses/{busId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/buses/{id} - Falha com 403 Forbidden quando usuário for STUDENT")
        void shouldReturnForbiddenWhenDeleteAsStudent() throws Exception {
            mockMvc.perform(delete("/api/buses/{busId}", UUID.randomUUID())
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/buses - Falha com 400 Bad Request quando nome for em branco (@NotBlank)")
        void shouldReturnBadRequestWhenBusNameIsBlankOnCreate() throws Exception {
            BusCreateDTO invalidDTO = new BusCreateDTO("", 44);

            mockMvc.perform(post("/api/buses")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/buses - Falha com 400 Bad Request quando quantidade de assentos for zero (@Min(1))")
        void shouldReturnBadRequestWhenSeatsQuantityIsZeroOnCreate() throws Exception {
            BusCreateDTO invalidDTO = new BusCreateDTO("Ônibus 01", 0);

            mockMvc.perform(post("/api/buses")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("POST /api/buses - Falha com 404 Not Found quando prefeitura não for encontrada")
        void shouldReturnNotFoundWhenMunicipalityNotFoundOnCreate() throws Exception {
            UUID userId = UUID.randomUUID();
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);

            when(busService.createBus(eq(userId), any(BusCreateDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não encontrada."));

            mockMvc.perform(post("/api/buses")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Prefeitura não encontrada."));
        }

        @Test
        @DisplayName("POST /api/buses - Falha com 409 Conflict quando ônibus já existir com mesmo nome")
        void shouldReturnConflictWhenBusNameAlreadyExistsOnCreate() throws Exception {
            UUID userId = UUID.randomUUID();
            BusCreateDTO requestDTO = new BusCreateDTO("Ônibus 01", 44);

            when(busService.createBus(eq(userId), any(BusCreateDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Já existe um ônibus cadastrado com este nome."));

            mockMvc.perform(post("/api/buses")
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0]").value("Já existe um ônibus cadastrado com este nome."));
        }

        @Test
        @DisplayName("GET /api/buses/{id} - Falha com 404 Not Found quando ônibus não for encontrado")
        void shouldReturnNotFoundWhenBusNotFoundOnGet() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();

            when(busService.getBusById(userId, busId))
                    .thenThrow(new ObjectNotFoundException("Ônibus não encontrado."));

            mockMvc.perform(get("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Ônibus não encontrado."));
        }

        @Test
        @DisplayName("GET /api/buses/{id} - Falha com 403 Forbidden quando ônibus for de outra prefeitura")
        void shouldReturnForbiddenWhenBusBelongsToOtherMunicipalityOnGet() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();

            when(busService.getBusById(userId, busId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para acessar este recurso."));

            mockMvc.perform(get("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors[0]").value("Você não tem permissão para acessar este recurso."));
        }

        @Test
        @DisplayName("PUT /api/buses/{id} - Falha com 400 Bad Request quando assentos for menor que 1 (@Min(1))")
        void shouldReturnBadRequestWhenSeatsQuantityInvalidOnUpdate() throws Exception {
            BusUpdateDTO invalidDTO = new BusUpdateDTO("Ônibus 01", -1);

            mockMvc.perform(put("/api/buses/{busId}", UUID.randomUUID())
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("PUT /api/buses/{id} - Falha com 409 Conflict quando novo nome já existir")
        void shouldReturnConflictWhenNewBusNameAlreadyExistsOnUpdate() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusUpdateDTO requestDTO = new BusUpdateDTO("Ônibus 02", 44);

            when(busService.updateBus(eq(userId), eq(busId), any(BusUpdateDTO.class)))
                    .thenThrow(new ObjectAlreadyExistsException("Já existe um ônibus cadastrado com este nome."));

            mockMvc.perform(put("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0]").value("Já existe um ônibus cadastrado com este nome."));
        }

        @Test
        @DisplayName("DELETE /api/buses/{id} - Falha com 400 Bad Request quando ônibus possuir viagens vinculadas")
        void shouldReturnBadRequestWhenBusHasTravelsOnDelete() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();

            doThrow(new IllegalArgumentException("Não é possível excluir este ônibus pois já existem viagens vinculadas a ele."))
                    .when(busService).deleteBus(userId, busId);

            mockMvc.perform(delete("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0]").value("Não é possível excluir este ônibus pois já existem viagens vinculadas a ele."));
        }

        @Test
        @DisplayName("DELETE /api/buses/{id} - Falha com 404 Not Found quando ônibus não existir")
        void shouldReturnNotFoundWhenBusNotFoundOnDelete() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();

            doThrow(new ObjectNotFoundException("Ônibus não encontrado."))
                    .when(busService).deleteBus(userId, busId);

            mockMvc.perform(delete("/api/buses/{busId}", busId)
                            .with(user(createMunicipalityPrincipal(userId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors[0]").value("Ônibus não encontrado."));
        }
    }
}
