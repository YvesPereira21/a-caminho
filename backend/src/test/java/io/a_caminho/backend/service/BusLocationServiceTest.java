package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.bus.BusLocationDTO;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.repository.BusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - BusLocationService")
class BusLocationServiceTest {

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private BusRepository busRepository;

    private BusLocationService busLocationService;

    @BeforeEach
    void setUp() {
        busLocationService = new BusLocationService(simpMessagingTemplate, busRepository);
    }

    @Nested
    @DisplayName("Testes de Atualização e Broadcast de Localização")
    class UpdateLocationTests {

        @Test
        @DisplayName("Deve atualizar o cache e enviar mensagem para os tópicos do WebSocket")
        void shouldUpdateLocationAndBroadcast() {
            UUID busId = UUID.randomUUID();
            float latitude = -6.8600f;
            float longitude = -35.4870f;

            busLocationService.updateLocation(busId, latitude, longitude);

            // Verifica cache em memória
            BusLocationDTO cached = busLocationService.getLatestLocation(busId);
            assertThat(cached).isNotNull();
            assertThat(cached.busId()).isEqualTo(busId);
            assertThat(cached.latitude()).isEqualTo(latitude);
            assertThat(cached.longitude()).isEqualTo(longitude);
            assertThat(cached.timestamp()).isNotNull();

            // Verifica envio aos tópicos
            ArgumentCaptor<BusLocationDTO> captor = ArgumentCaptor.forClass(BusLocationDTO.class);
            verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/topic/buses/" + busId + "/location"), captor.capture());
            verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/topic/live-location"), captor.capture());

            BusLocationDTO sentDto = captor.getValue();
            assertThat(sentDto.busId()).isEqualTo(busId);
            assertThat(sentDto.latitude()).isEqualTo(latitude);
            assertThat(sentDto.longitude()).isEqualTo(longitude);
        }
    }

    @Nested
    @DisplayName("Testes de Consulta de Localização")
    class QueryLocationTests {

        @Test
        @DisplayName("Deve retornar nulo quando ônibus não possui coordenadas registradas")
        void shouldReturnNullWhenBusHasNoLocation() {
            UUID busId = UUID.randomUUID();
            BusLocationDTO result = busLocationService.getLatestLocation(busId);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve retornar todas as localizações registradas no cache")
        void shouldReturnAllLocations() {
            UUID bus1 = UUID.randomUUID();
            UUID bus2 = UUID.randomUUID();

            busLocationService.updateLocation(bus1, -6.86f, -35.48f);
            busLocationService.updateLocation(bus2, -7.12f, -34.88f);

            Map<UUID, BusLocationDTO> allLocations = busLocationService.getAllLocations();
            assertThat(allLocations).hasSize(2);
            assertThat(allLocations).containsKey(bus1);
            assertThat(allLocations).containsKey(bus2);
        }

        @Test
        @DisplayName("Deve filtrar e retornar apenas localizações de ônibus da frota da prefeitura")
        void shouldReturnOnlyBusesFromMunicipality() {
            UUID muniUserId = UUID.randomUUID();
            UUID muniBusId = UUID.randomUUID();
            UUID otherBusId = UUID.randomUUID();

            Bus muniBus = Bus.builder().busId(muniBusId).busName("Ônibus 01").build();
            when(busRepository.findAllByMunicipality_User_UserId(muniUserId)).thenReturn(List.of(muniBus));

            busLocationService.updateLocation(muniBusId, -6.86f, -35.48f);
            busLocationService.updateLocation(otherBusId, -7.12f, -34.88f);

            Map<UUID, BusLocationDTO> muniLocations = busLocationService.getLocationsForMunicipalityUser(muniUserId);
            assertThat(muniLocations).hasSize(1);
            assertThat(muniLocations).containsKey(muniBusId);
            assertThat(muniLocations).doesNotContainKey(otherBusId);
        }
    }
}

