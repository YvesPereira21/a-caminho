package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.travel.TravelCancelDTO;
import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.exception.CrossMunicipalityAccessException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.TravelStatus;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.TravelService;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - TravelController")
class TravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private TravelService travelService;

    private UserPrincipal createBusDriverPrincipal(UUID userId) {
        return UserPrincipal.create(User.builder()
                .userId(userId)
                .email("motorista@municipio.gov.br")
                .role(UserRole.BUS_DRIVER)
                .build());
    }

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

    private TravelDTO createSampleTravelDTO(UUID travelId, PollDirection direction) {
        BusDriverResponseDTO driverResponse = new BusDriverResponseDTO(
                UUID.randomUUID(),
                "Severino Motorista",
                "motorista@municipio.gov.br"
        );
        return new TravelDTO(
                travelId,
                direction,
                driverResponse,
                20,
                Collections.emptySet()
        );
    }

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/travels - Inicia uma nova viagem com sucesso (201 Created)")
        void shouldCreateTravelSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelCreateDTO requestDTO = new TravelCreateDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TravelStatus.IN_PROGRESS,
                    LocalTime.of(22, 30)
            );
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.createTravel(eq(driverUserId), any(TravelCreateDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/travels")
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()))
                    .andExpect(jsonPath("$.direction").value("Ida"));

            verify(travelService).createTravel(eq(driverUserId), any(TravelCreateDTO.class));
        }

        @Test
        @DisplayName("GET /api/travels/{id} - Busca viagem por ID com sucesso pelo motorista (200 OK)")
        void shouldGetTravelByIdAsDriverSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.getTravelById(travelId, driverUserId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/travels/{travelId}", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()));

            verify(travelService).getTravelById(travelId, driverUserId);
        }

        @Test
        @DisplayName("GET /api/travels/{id} - Busca viagem por ID com sucesso pela prefeitura (200 OK)")
        void shouldGetTravelByIdAsMunicipalitySuccessfully() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.getTravelById(travelId, municipalityUserId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/travels/{travelId}", travelId)
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()));

            verify(travelService).getTravelById(travelId, municipalityUserId);
        }

        @Test
        @DisplayName("GET /api/travels/active - Busca viagem em andamento do motorista com sucesso (200 OK)")
        void shouldGetActiveTravelByDriverSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.getActiveTravelByDriver(driverUserId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/travels/active")
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()));

            verify(travelService).getActiveTravelByDriver(driverUserId);
        }

        @Test
        @DisplayName("GET /api/travels/driver - Lista viagens do motorista com sucesso (200 OK)")
        void shouldGetAllTravelsByDriverSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.getAllTravelsByDriver(driverUserId)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/travels/driver")
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].travelId").value(travelId.toString()));

            verify(travelService).getAllTravelsByDriver(driverUserId);
        }

        @Test
        @DisplayName("GET /api/travels/municipality - Lista viagens da prefeitura com sucesso (200 OK)")
        void shouldGetAllTravelsByMunicipalitySuccessfully() throws Exception {
            UUID municipalityUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.getAllTravelsByMunicipality(municipalityUserId)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/travels/municipality")
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].travelId").value(travelId.toString()));

            verify(travelService).getAllTravelsByMunicipality(municipalityUserId);
        }

        @Test
        @DisplayName("GET /api/travels/{id}/passengers - Lista passageiros da viagem com sucesso (200 OK)")
        void shouldGetTravelPassengersSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            PollPassengerDTO passengerDTO = new PollPassengerDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Ana Silva",
                    "2021001",
                    "Engenharia",
                    UUID.randomUUID(),
                    "Ponto Central",
                    true,
                    LocalDateTime.now()
            );

            when(travelService.getTravelPassengers(travelId, driverUserId)).thenReturn(List.of(passengerDTO));

            mockMvc.perform(get("/api/travels/{travelId}/passengers", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].studentName").value("Ana Silva"))
                    .andExpect(jsonPath("$[0].stopName").value("Ponto Central"));

            verify(travelService).getTravelPassengers(travelId, driverUserId);
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/start-return - Inicia retorno com sucesso pelo motorista (200 OK)")
        void shouldStartReturnSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.INBOUND);

            when(travelService.startReturn(travelId, driverUserId)).thenReturn(responseDTO);

            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()))
                    .andExpect(jsonPath("$.direction").value("Volta"));

            verify(travelService).startReturn(travelId, driverUserId);
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/complete - Conclui a viagem com sucesso pelo motorista (200 OK)")
        void shouldCompleteTravelSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.INBOUND);

            when(travelService.completeTravel(travelId, driverUserId)).thenReturn(responseDTO);

            mockMvc.perform(patch("/api/travels/{travelId}/complete", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()));

            verify(travelService).completeTravel(travelId, driverUserId);
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/cancel - Cancela a viagem com sucesso (200 OK)")
        void shouldCancelTravelSuccessfully() throws Exception {
            UUID driverUserId = UUID.randomUUID();
            UUID travelId = UUID.randomUUID();
            TravelCancelDTO requestDTO = new TravelCancelDTO("Problema no motor");
            TravelDTO responseDTO = createSampleTravelDTO(travelId, PollDirection.OUTBOUND);

            when(travelService.cancelTravel(travelId, driverUserId, "Problema no motor"))
                    .thenReturn(responseDTO);

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelId").value(travelId.toString()));

            verify(travelService).cancelTravel(travelId, driverUserId, "Problema no motor");
        }
    }

    @Nested
    @DisplayName("Cenários de Autenticação e Autorização (Sad Path)")
    class SecuritySadPathTests {

        private final UUID travelId = UUID.randomUUID();

        @Test
        @DisplayName("Deve retornar 401 Unauthorized para todas as rotas quando a requisição não estiver autenticada")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            TravelCreateDTO createDTO = new TravelCreateDTO(UUID.randomUUID(), UUID.randomUUID(), TravelStatus.IN_PROGRESS, LocalTime.now());
            TravelCancelDTO cancelDTO = new TravelCancelDTO("Cancelamento");

            mockMvc.perform(post("/api/travels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/travels/{travelId}", travelId))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/travels/active"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/travels/driver"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/travels/municipality"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/travels/{travelId}/passengers", travelId))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(patch("/api/travels/{travelId}/complete", travelId))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/travels - Deve retornar 403 Forbidden para prefeitura ou estudante")
        void shouldReturn403WhenNonDriverCreatesTravel() throws Exception {
            TravelCreateDTO createDTO = new TravelCreateDTO(UUID.randomUUID(), UUID.randomUUID(), TravelStatus.IN_PROGRESS, LocalTime.now());

            mockMvc.perform(post("/api/travels")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/travels")
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/travels/{id} - Deve retornar 403 Forbidden para estudante")
        void shouldReturn403WhenStudentAccessesTravelById() throws Exception {
            mockMvc.perform(get("/api/travels/{travelId}", travelId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/travels/active - Deve retornar 403 Forbidden para prefeitura ou estudante")
        void shouldReturn403WhenNonDriverAccessesActiveTravel() throws Exception {
            mockMvc.perform(get("/api/travels/active")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/travels/active")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/travels/driver - Deve retornar 403 Forbidden para prefeitura ou estudante")
        void shouldReturn403WhenNonDriverAccessesDriverTravels() throws Exception {
            mockMvc.perform(get("/api/travels/driver")
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/travels/driver")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/travels/municipality - Deve retornar 403 Forbidden para motorista ou estudante")
        void shouldReturn403WhenNonMunicipalityAccessesMunicipalityTravels() throws Exception {
            mockMvc.perform(get("/api/travels/municipality")
                            .with(user(createBusDriverPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/travels/municipality")
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/travels/{id}/passengers - Deve retornar 403 Forbidden para estudante")
        void shouldReturn403WhenStudentAccessesTravelPassengers() throws Exception {
            mockMvc.perform(get("/api/travels/{travelId}/passengers", travelId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/start-return - Deve retornar 403 Forbidden para prefeitura ou estudante")
        void shouldReturn403WhenNonDriverStartsReturn() throws Exception {
            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId)
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/complete - Deve retornar 403 Forbidden para prefeitura ou estudante")
        void shouldReturn403WhenNonDriverCompletesTravel() throws Exception {
            mockMvc.perform(patch("/api/travels/{travelId}/complete", travelId)
                            .with(user(createMunicipalityPrincipal(UUID.randomUUID()))))
                    .andExpect(status().isForbidden());

            mockMvc.perform(patch("/api/travels/{travelId}/complete", travelId)
                            .with(user(createStudentPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/cancel - Deve retornar 403 Forbidden para estudante")
        void shouldReturn403WhenStudentCancelsTravel() throws Exception {
            TravelCancelDTO cancelDTO = new TravelCancelDTO("Motivo qualquer");

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .with(user(createStudentPrincipal()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        private final UUID travelId = UUID.randomUUID();
        private final UUID driverUserId = UUID.randomUUID();
        private final UUID municipalityUserId = UUID.randomUUID();

        @Test
        @DisplayName("POST /api/travels - Deve retornar 400 Bad Request quando payload violar Bean Validation (@NotNull)")
        void shouldReturn400WhenPayloadViolatesValidation() throws Exception {
            String invalidPayload = "{}";

            mockMvc.perform(post("/api/travels")
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("POST /api/travels - Deve retornar 400 Bad Request quando motorista ou ônibus já tiver viagem em andamento")
        void shouldReturn400WhenDriverOrBusAlreadyInProgress() throws Exception {
            TravelCreateDTO requestDTO = new TravelCreateDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TravelStatus.IN_PROGRESS,
                    LocalTime.of(22, 0)
            );

            when(travelService.createTravel(eq(driverUserId), any(TravelCreateDTO.class)))
                    .thenThrow(new IllegalArgumentException("O motorista já possui uma viagem em andamento."));

            mockMvc.perform(post("/api/travels")
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors[0]").value("O motorista já possui uma viagem em andamento."));
        }

        @Test
        @DisplayName("POST /api/travels - Deve retornar 403 Forbidden quando recurso pertencer a outro município")
        void shouldReturn403WhenResourceFromAnotherMunicipalityOnCreate() throws Exception {
            TravelCreateDTO requestDTO = new TravelCreateDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TravelStatus.IN_PROGRESS,
                    LocalTime.of(22, 0)
            );

            when(travelService.createTravel(eq(driverUserId), any(TravelCreateDTO.class)))
                    .thenThrow(new CrossMunicipalityAccessException("A enquete selecionada não pertence ao seu município."));

            mockMvc.perform(post("/api/travels")
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.errors[0]").value("A enquete selecionada não pertence ao seu município."));
        }

        @Test
        @DisplayName("POST /api/travels - Deve retornar 404 Not Found quando enquete ou ônibus não forem encontrados")
        void shouldReturn404WhenEntityNotFoundOnCreate() throws Exception {
            TravelCreateDTO requestDTO = new TravelCreateDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TravelStatus.IN_PROGRESS,
                    LocalTime.of(22, 0)
            );

            when(travelService.createTravel(eq(driverUserId), any(TravelCreateDTO.class)))
                    .thenThrow(new ObjectNotFoundException("Enquete não encontrada."));

            mockMvc.perform(post("/api/travels")
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.errors[0]").value("Enquete não encontrada."));
        }

        @Test
        @DisplayName("GET /api/travels/{id} - Deve retornar 404 Not Found quando a viagem não for encontrada")
        void shouldReturn404WhenTravelNotFound() throws Exception {
            when(travelService.getTravelById(travelId, driverUserId))
                    .thenThrow(new ObjectNotFoundException("Viagem não encontrada."));

            mockMvc.perform(get("/api/travels/{travelId}", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("GET /api/travels/{id} - Deve retornar 403 Forbidden quando usuário for de outro município")
        void shouldReturn403WhenCrossMunicipalityOnGetTravel() throws Exception {
            when(travelService.getTravelById(travelId, driverUserId))
                    .thenThrow(new CrossMunicipalityAccessException("Você não tem permissão para visualizar os dados desta viagem."));

            mockMvc.perform(get("/api/travels/{travelId}", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("GET /api/travels/active - Deve retornar 404 Not Found quando não houver viagem em andamento")
        void shouldReturn404WhenNoActiveTravelFound() throws Exception {
            when(travelService.getActiveTravelByDriver(driverUserId))
                    .thenThrow(new ObjectNotFoundException("Nenhuma viagem em andamento encontrada para este motorista."));

            mockMvc.perform(get("/api/travels/active")
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("GET /api/travels/municipality - Deve retornar 404 Not Found quando prefeitura não for encontrada")
        void shouldReturn404WhenMunicipalityNotFound() throws Exception {
            when(travelService.getAllTravelsByMunicipality(municipalityUserId))
                    .thenThrow(new ObjectNotFoundException("Prefeitura não encontrada."));

            mockMvc.perform(get("/api/travels/municipality")
                            .with(user(createMunicipalityPrincipal(municipalityUserId))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/start-return - Deve retornar 400 Bad Request quando a viagem não puder iniciar retorno")
        void shouldReturn400WhenStartReturnRuleFails() throws Exception {
            when(travelService.startReturn(travelId, driverUserId))
                    .thenThrow(new IllegalArgumentException("Apenas viagens em andamento podem iniciar o retorno."));

            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors[0]").value("Apenas viagens em andamento podem iniciar o retorno."));
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/start-return - Deve retornar 403 Forbidden quando usuário não for o motorista responsável")
        void shouldReturn403WhenNotResponsibleDriverOnStartReturn() throws Exception {
            when(travelService.startReturn(travelId, driverUserId))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para alterar esta viagem pois não é o motorista responsável."));

            mockMvc.perform(patch("/api/travels/{travelId}/start-return", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/complete - Deve retornar 400 Bad Request quando a viagem não puder ser concluída")
        void shouldReturn400WhenCompleteTravelRuleFails() throws Exception {
            when(travelService.completeTravel(travelId, driverUserId))
                    .thenThrow(new IllegalArgumentException("Apenas viagens em andamento podem ser concluídas."));

            mockMvc.perform(patch("/api/travels/{travelId}/complete", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors[0]").value("Apenas viagens em andamento podem ser concluídas."));
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/cancel - Deve retornar 400 Bad Request quando cancellationReason for em branco (@NotBlank)")
        void shouldReturn400WhenCancellationReasonIsBlank() throws Exception {
            TravelCancelDTO requestDTO = new TravelCancelDTO("   ");

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/cancel - Deve retornar 400 Bad Request quando viagem já concluída ou cancelada")
        void shouldReturn400WhenTravelAlreadyCompletedOrCancelled() throws Exception {
            TravelCancelDTO requestDTO = new TravelCancelDTO("Motivo válido");

            when(travelService.cancelTravel(travelId, driverUserId, "Motivo válido"))
                    .thenThrow(new IllegalArgumentException("Não é possível cancelar uma viagem que já foi concluída ou cancelada."));

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("PATCH /api/travels/{id}/cancel - Deve retornar 403 Forbidden quando usuário não tiver permissão para cancelar")
        void shouldReturn403WhenUserHasNoCancellationPermission() throws Exception {
            TravelCancelDTO requestDTO = new TravelCancelDTO("Motivo válido");

            when(travelService.cancelTravel(travelId, driverUserId, "Motivo válido"))
                    .thenThrow(new UserIsNotOwnerException("Você não tem permissão para cancelar esta viagem."));

            mockMvc.perform(patch("/api/travels/{travelId}/cancel", travelId)
                            .with(user(createBusDriverPrincipal(driverUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(403));
        }
    }
}
