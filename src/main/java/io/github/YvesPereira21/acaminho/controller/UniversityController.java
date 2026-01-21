package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.UniversityDTO;
import io.github.YvesPereira21.acaminho.mapper.UniversityMapper;
import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.service.UniversityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/universities")
public class UniversityController {

    private final UniversityService universityService;
    private final UniversityMapper universityMapper;

    public UniversityController(UniversityService universityService, UniversityMapper universityMapper) {
        this.universityService = universityService;
        this.universityMapper = universityMapper;
    }

    @PostMapping("")
    public ResponseEntity<UniversityDTO> createUniversity(@RequestBody UniversityDTO universityDTO) {
        University university = universityMapper.convertToEntity(universityDTO);
        University newUniversity = universityService.createUniversity(university);
        return ResponseEntity.status(HttpStatus.CREATED).body(universityMapper.convertToDTO(newUniversity));
    }

    @GetMapping("/{universityName}")
    public ResponseEntity<List<UniversityDTO>> getAllUniversityByNameContaining(@PathVariable String universityName) {
        List<UniversityDTO> universities = universityService.allUniversityContainingName(universityName)
                .stream()
                .map(universityMapper::convertToDTO)
                .toList();
        return ResponseEntity.ok(universities);
    }

    @DeleteMapping("/{universityId}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable("universityId") UUID universityId) {
        universityService.deleteUniversity(universityId);
        return ResponseEntity.noContent().build();
    }
}