package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.UniversityControllerOpenApi;
import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.service.UniversityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityController implements UniversityControllerOpenApi {

    private final UniversityService universityService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UniversityResponseDTO> createUniversity(
            @Valid @RequestBody UniversityRequestDTO requestDTO) {
        log.info("Request to create university: {} in {}, {}", requestDTO.name(), requestDTO.cityName(), requestDTO.stateName());
        UniversityResponseDTO createdUniversity = universityService.createUniversity(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUniversity);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversityByNameFromState(
            @RequestParam String universityName,
            @RequestParam String stateName) {
        log.info("Request to fetch universities by name: {} and state: {}", universityName, stateName);
        List<UniversityResponseDTO> universities = universityService.getAllUniversityByNameFromState(universityName, stateName);
        return ResponseEntity.ok(universities);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{universityId}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID universityId) {
        log.info("Request to delete university with ID: {}", universityId);
        universityService.deleteUniversity(universityId);
        return ResponseEntity.noContent().build();
    }
}
