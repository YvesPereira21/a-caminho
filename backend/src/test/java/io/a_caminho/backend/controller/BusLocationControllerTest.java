package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.bus.BusLocationDTO;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.BusLocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - BusLocationController")
class BusLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusLocationController busLocationController;

    @MockitoBean
    private BusLocationService busLocationService;

    private UserPrincipal createStudentPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("estudante@acaminho.com")
                .role(UserRole.STUDENT)
                .password("password123")
                .build());
    }

    private UserPrincipal createMunicipalityPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("prefeitura@acaminho.com")
                .role(UserRole.MUNICIPALITY)
                .password("password123")
                .build());
    }

    private UserPrincipal createAdminPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("admin@acaminho.com")
                .role(UserRole.ADMIN)
                .password("password123")
                .build());
    }

    @Nested
    @DisplayName("Caminho Feliz - Happy Path")
    class HappyPathTests {

        @Test
        @DisplayName("GET /api/buses/{busId}/location - Deve retornar 200 OK com dados da localização")
        void shouldReturnLatestLocation() throws Exception {
            UUID busId = UUID.randomUUID();
            BusLocationDTO locationDTO = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            when(busLocationService.getLatestLocation(busId)).thenReturn(locationDTO);

            mockMvc.perform(get("/api/buses/{busId}/location", busId)
                            .with(user(createStudentPrincipal(UUID.randomUUID())))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.busId").value(busId.toString()))
                    .andExpect(jsonPath("$.latitude").value(-6.8600))
                    .andExpect(jsonPath("$.longitude").value(-35.4870));
        }

        @Test
        @DisplayName("GET /api/buses/{busId}/location - Deve retornar 204 No Content se não houver coordenadas")
        void shouldReturnNoContentWhenLocationNull() throws Exception {
            UUID busId = UUID.randomUUID();
            when(busLocationService.getLatestLocation(busId)).thenReturn(null);

            mockMvc.perform(get("/api/buses/{busId}/location", busId)
                            .with(user(createStudentPrincipal(UUID.randomUUID())))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("GET /api/buses/locations - Deve retornar 200 OK filtrado para prefeitura")
        void shouldReturnLocationsForMunicipality() throws Exception {
            UUID muniUserId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusLocationDTO locationDTO = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            when(busLocationService.getLocationsForMunicipalityUser(muniUserId)).thenReturn(Map.of(busId, locationDTO));

            mockMvc.perform(get("/api/buses/locations")
                            .with(user(createMunicipalityPrincipal(muniUserId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + busId.toString() + ".busId").value(busId.toString()));
        }

        @Test
        @DisplayName("GET /api/buses/locations - Deve retornar 200 OK geral para administrador")
        void shouldReturnAllLocationsForAdmin() throws Exception {
            UUID adminUserId = UUID.randomUUID();
            UUID busId = UUID.randomUUID();
            BusLocationDTO locationDTO = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            when(busLocationService.getAllLocations()).thenReturn(Map.of(busId, locationDTO));

            mockMvc.perform(get("/api/buses/locations")
                            .with(user(createAdminPrincipal(adminUserId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + busId.toString() + ".busId").value(busId.toString()));
        }
    }

    @Nested
    @DisplayName("Segurança - Security Sad Path")
    class SecuritySadPathTests {

        @Test
        @DisplayName("GET /api/buses/{busId}/location - Deve retornar 401 Unauthorized para usuário não autenticado")
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            UUID busId = UUID.randomUUID();
            mockMvc.perform(get("/api/buses/{busId}/location", busId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/buses/locations - Deve retornar 403 Forbidden para estudante")
        void shouldReturnForbiddenForStudentWhenAccessingAllLocations() throws Exception {
            mockMvc.perform(get("/api/buses/locations")
                            .with(user(createStudentPrincipal(UUID.randomUUID())))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/buses/locations - Deve retornar 401 Unauthorized para usuário não autenticado")
        void shouldReturnUnauthorizedWhenAccessingAllLocationsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/buses/locations")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Segurança do Método WebSocket - @PreAuthorize no handleLocationUpdate")
    class StompMethodSecurityTests {

        @Test
        @WithMockUser(roles = "BUS_DRIVER")
        @DisplayName("handleLocationUpdate - Deve permitir execução quando usuário possui ROLE_BUS_DRIVER")
        void shouldAllowUpdateWhenBusDriver() {
            UUID busId = UUID.randomUUID();
            BusLocationDTO dto = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            busLocationController.handleLocationUpdate(dto);

            verify(busLocationService).updateLocation(busId, -6.8600f, -35.4870f);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("handleLocationUpdate - Deve lançar AccessDeniedException quando usuário possui ROLE_STUDENT")
        void shouldThrowAccessDeniedWhenStudent() {
            UUID busId = UUID.randomUUID();
            BusLocationDTO dto = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            assertThatThrownBy(() -> busLocationController.handleLocationUpdate(dto))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("handleLocationUpdate - Deve lançar exceção quando usuário não está autenticado")
        void shouldThrowExceptionWhenUnauthenticated() {
            UUID busId = UUID.randomUUID();
            BusLocationDTO dto = new BusLocationDTO(busId, -6.8600f, -35.4870f, LocalDateTime.now());

            assertThatThrownBy(() -> busLocationController.handleLocationUpdate(dto))
                    .isInstanceOf(Exception.class);
        }
    }
}

