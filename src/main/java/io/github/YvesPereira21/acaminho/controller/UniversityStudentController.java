package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.UniversityStudentRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityStudentResponseDTO;
import io.github.YvesPereira21.acaminho.service.UniversityStudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/universityStudents")
public class UniversityStudentController {

    private final UniversityStudentService universityStudentService;

    public UniversityStudentController(UniversityStudentService universityStudentService) {
        this.universityStudentService = universityStudentService;
    }

    @PostMapping("")
    public ResponseEntity<UniversityStudentResponseDTO> createUniversityStudent(@RequestBody UniversityStudentRequestDTO universityStudentRequestDTO) {
        UniversityStudentResponseDTO university = universityStudentService.createUniversityStudentAccount(universityStudentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(university);
    }

    @GetMapping("/{universityStudentId}")
    public ResponseEntity<UniversityStudentResponseDTO> getUniversityStudentById(@PathVariable UUID universityStudentId) {
        return ResponseEntity.ok(universityStudentService.getUniversityStudentById(universityStudentId));
    }

    @DeleteMapping("/{universityStudentId}")
    public ResponseEntity<Void> deleteUniversityStudentById(@PathVariable UUID universityStudentId) {
        universityStudentService.deleteUniversityStudentById(universityStudentId);
        return ResponseEntity.noContent().build();
    }
}
