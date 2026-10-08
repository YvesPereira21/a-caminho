package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.UniversityStudentControllerOpenApi;
import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.service.UniversityStudentService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping({"/api/university-students", "/api/students"})
@RequiredArgsConstructor
public class UniversityStudentController implements UniversityStudentControllerOpenApi {

    private final UniversityStudentService universityStudentService;

    @Override
    @PostMapping
    public ResponseEntity<StudentDTO> registerStudent(@Valid @RequestBody StudentCreateDTO request) {
        log.info("Student registration attempt for email: {}, CPF: {}", request.email(), request.cpf());
        StudentDTO response = universityStudentService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable UUID studentId) {
        log.info("Fetching student details for ID: {}", studentId);
        StudentDTO studentDTO = universityStudentService.getStudent(studentId);
        return ResponseEntity.ok(studentDTO);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/{studentId}")
    public ResponseEntity<Void> updateStudent(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentUpdateDTO updateDTO) {
        log.info("Update student request for student ID: {} by user ID: {}", studentId, userId);
        universityStudentService.updateStudent(userId, studentId, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudentAccount(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID studentId) {
        log.info("Delete student request for student ID: {} by user ID: {}", studentId, userId);
        universityStudentService.deleteStudentAccount(userId, studentId);
        return ResponseEntity.noContent().build();
    }
}
