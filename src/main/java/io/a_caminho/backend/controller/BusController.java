package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.BusControllerOpenApi;
import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController implements BusControllerOpenApi {

    private final BusService busService;

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PostMapping
    public ResponseEntity<BusDTO> createBus(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody BusCreateDTO busCreateDTO
    ) {
        log.info("Request to create bus '{}' for municipality user ID: {}", busCreateDTO.busName(), municipalityUserId);
        BusDTO createdBus = busService.createBus(municipalityUserId, busCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBus);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/{busId}")
    public ResponseEntity<BusDTO> getBusById(
            @PathVariable UUID busId,
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        BusDTO bus = busService.getBusById(municipalityUserId, busId);
        return ResponseEntity.ok(bus);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping
    public ResponseEntity<List<BusDTO>> getAllBusesByMunicipality(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all buses for municipality user ID: {}", municipalityUserId);
        List<BusDTO> buses = busService.getAllBusesByMunicipality(municipalityUserId);
        return ResponseEntity.ok(buses);
    }

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @GetMapping("/driver")
    public ResponseEntity<List<BusDTO>> getAllBusesForDriver(
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch all buses for bus driver user ID: {}", busDriverUserId);
        List<BusDTO> buses = busService.getAllBusesForDriver(busDriverUserId);
        return ResponseEntity.ok(buses);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PutMapping("/{busId}")
    public ResponseEntity<BusDTO> updateBus(
            @PathVariable UUID busId,
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody BusUpdateDTO busUpdateDTO
    ) {
        log.info("Request to update bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        BusDTO updatedBus = busService.updateBus(municipalityUserId, busId, busUpdateDTO);
        return ResponseEntity.ok(updatedBus);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @DeleteMapping("/{busId}")
    public ResponseEntity<Void> deleteBus(
            @PathVariable UUID busId,
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to delete bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        busService.deleteBus(municipalityUserId, busId);
        return ResponseEntity.noContent().build();
    }
}
