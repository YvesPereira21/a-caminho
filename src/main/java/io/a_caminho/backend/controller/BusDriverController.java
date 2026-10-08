package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.BusDriverControllerOpenApi;
import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.service.BusDriverService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bus-drivers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MUNICIPALITY')")
public class BusDriverController implements BusDriverControllerOpenApi {

    private final BusDriverService busDriverService;

    @Override
    @PostMapping
    public ResponseEntity<BusDriverResponseDTO> createBusDriver(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody BusDriverRequestDTO requestDTO) {
        log.info("Request to create bus driver for municipality user ID: {}", municipalityUserId);
        BusDriverResponseDTO createdDriver = busDriverService.createBusDriverAccount(requestDTO, municipalityUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
    }

    @Override
    @GetMapping("/{busDriverId}")
    public ResponseEntity<BusDriverResponseDTO> getBusDriverById(
            @PathVariable UUID busDriverId,
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId) {
        log.info("Request to fetch bus driver ID: {} by municipality user ID: {}", busDriverId, municipalityUserId);
        BusDriverResponseDTO driver = busDriverService.getBusDriverById(busDriverId, municipalityUserId);
        return ResponseEntity.ok(driver);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<BusDriverResponseDTO>> getAllBusDriversFromMunicipality(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId) {
        log.info("Request to fetch all bus drivers for municipality user ID: {}", municipalityUserId);
        List<BusDriverResponseDTO> drivers = busDriverService.getAllBusDriversFromMunicipality(municipalityUserId);
        return ResponseEntity.ok(drivers);
    }

    @Override
    @DeleteMapping("/{busDriverId}")
    public ResponseEntity<Void> deleteBusDriverAccount(
            @PathVariable UUID busDriverId,
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId) {
        log.info("Request to delete bus driver ID: {} by municipality user ID: {}", busDriverId, municipalityUserId);
        busDriverService.deleteBusDriverAccount(busDriverId, municipalityUserId);
        return ResponseEntity.noContent().build();
    }
}
