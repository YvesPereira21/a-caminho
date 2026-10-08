package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.MunicipalityControllerOpenApi;
import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.service.MunicipalityService;
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
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
public class MunicipalityController implements MunicipalityControllerOpenApi {

    private final MunicipalityService municipalityService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MunicipalityResponseDTO> createMunicipality(
            @Valid @RequestBody MunicipalityRequestDTO municipalityRequestDTO) {
        log.info("Request to create municipality: {}", municipalityRequestDTO.municipalityName());
        MunicipalityResponseDTO createdMunicipality = municipalityService.createMunicipality(municipalityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMunicipality);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/{municipalityName}")
    public ResponseEntity<MunicipalityResponseDTO> getMunicipalityByName(@PathVariable String municipalityName) {
        log.info("Request to fetch municipality by name: {}", municipalityName);
        MunicipalityResponseDTO municipality = municipalityService.getMunicipalityByName(municipalityName);
        return ResponseEntity.ok(municipality);
    }

    @Override
    @GetMapping("/state/{stateName}")
    public ResponseEntity<List<MunicipalityResponseDTO>> getAllMunicipalityFromState(@PathVariable String stateName) {
        log.info("Request to fetch all municipalities for state: {}", stateName);
        List<MunicipalityResponseDTO> municipalities = municipalityService.getAllMunicipalityFromState(stateName);
        return ResponseEntity.ok(municipalities);
    }

    @Override
    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'ADMIN')")
    @DeleteMapping("/{municipalityName}")
    public ResponseEntity<Void> deleteMunicipality(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable String municipalityName) {
        log.info("Request to delete municipality: {} by user ID: {}", municipalityName, municipalityUserId);
        municipalityService.deleteMunicipality(municipalityUserId, municipalityName);
        return ResponseEntity.noContent().build();
    }
}
