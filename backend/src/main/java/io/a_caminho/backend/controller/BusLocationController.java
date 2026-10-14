package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.BusLocationControllerOpenApi;
import io.a_caminho.backend.dto.bus.BusLocationDTO;
import io.a_caminho.backend.service.BusLocationService;
import io.a_caminho.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusLocationController implements BusLocationControllerOpenApi {

    private final BusLocationService busLocationService;

    /**
     * Canal STOMP para recebimento de coordenadas via WebSocket.
     * Rota de envio pelo cliente: /app/location
     * Somente motoristas autenticados (ROLE_BUS_DRIVER) possuem permissão.
     */
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @MessageMapping("/location")
    public void handleLocationUpdate(@Valid @Payload BusLocationDTO busLocationDTO) {
        log.debug("Received STOMP location update for bus {}", busLocationDTO.busId());
        busLocationService.updateLocation(
                busLocationDTO.busId(),
                busLocationDTO.latitude(),
                busLocationDTO.longitude()
        );
    }

    /**
     * Endpoint REST HTTP para carga inicial do mapa no frontend.
     */
    @Override
    @GetMapping("/{busId}/location")
    public ResponseEntity<BusLocationDTO> getLatestLocation(@PathVariable UUID busId) {
        log.info("Request to fetch latest location for bus ID: {}", busId);
        BusLocationDTO location = busLocationService.getLatestLocation(busId);
        if (location == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(location);
    }

    /**
     * Endpoint REST HTTP para listar as localizações em tempo real dos ônibus.
     * Restrito a Prefeituras (frota do município) e Administradores (visão global).
     */
    @Override
    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'ADMIN')")
    @GetMapping("/locations")
    public ResponseEntity<Map<UUID, BusLocationDTO>> getAllLocations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("Request to fetch active bus locations by user ID: {}", currentUser.getId());

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        Map<UUID, BusLocationDTO> locations = isAdmin
                ? busLocationService.getAllLocations()
                : busLocationService.getLocationsForMunicipalityUser(currentUser.getId());

        return ResponseEntity.ok(locations);
    }
}

