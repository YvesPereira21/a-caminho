package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.UniversityStudentDTO;
import io.github.YvesPereira21.acaminho.mapper.UniversityStudentMapper;
import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import io.github.YvesPereira21.acaminho.service.UniversityStudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/students")
public class UniversityStudentController {

    private final UniversityStudentService universityStudentService;
    private final UniversityStudentMapper universityStudentMapper;

    public UniversityStudentController(UniversityStudentService universityStudentService, UniversityStudentMapper universityStudentMapper) {
        this.universityStudentService = universityStudentService;
        this.universityStudentMapper = universityStudentMapper;
    }

    @PostMapping("")
    public ResponseEntity<UniversityStudentDTO> createUniversityStudent(@Valid @RequestBody UniversityStudentDTO universityStudentDTO) {
        UniversityStudent student = universityStudentMapper.convertToEntity(universityStudentDTO);
        UniversityStudent newStudent = universityStudentService.createUniversityStudentAccount(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(universityStudentMapper.convertToDTO(newStudent));
    }

    @GetMapping("/{universityStudentId}")
    public ResponseEntity<UniversityStudentDTO> getUniversityStudent(@PathVariable UUID universityStudentId) {
        UniversityStudent student = universityStudentService.getUniversityStudentById(universityStudentId);
        return ResponseEntity.ok(universityStudentMapper.convertToDTO(student));
    }

    @DeleteMapping("/{universityStudentId}")
    public ResponseEntity<Void> deleteUniversityStudent(@PathVariable UUID universityStudentId) {
        universityStudentService.deleteUniversityStudentById(universityStudentId);
        return ResponseEntity.noContent().build();
    }
}