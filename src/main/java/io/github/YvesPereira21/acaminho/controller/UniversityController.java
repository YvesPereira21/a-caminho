package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.UniversityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityResponseDTO;
import io.github.YvesPereira21.acaminho.service.UniversityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/universities")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping("")
    public ResponseEntity<UniversityResponseDTO> createUniversity(@RequestBody UniversityRequestDTO universityRequestDTO){
        UniversityResponseDTO university =  universityService.createUniversity(universityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(university);
    }

    @GetMapping("")
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversityContainingName(@PathVariable String universityName){
        return ResponseEntity.ok(universityService.allUniversityContainingName(universityName));
    }

    @DeleteMapping("/{universityId}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID universityId){
        universityService.deleteUniversity(universityId);
        return ResponseEntity.noContent().build();
    }
}
