package io.a_caminho.backend.controller;

import io.a_caminho.backend.dto.student.StudentRegistrationRequestDTO;
import io.a_caminho.backend.dto.student.StudentResponseDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.UniversityStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({"/api/university-students", "/api/students"})
@RequiredArgsConstructor
@Tag(name = "Estudantes Universitários", description = "Endpoints de gerenciamento e auto-cadastro de estudantes universitários")
public class UniversityStudentController {

    private final UniversityStudentService universityStudentService;

    @Operation(
            summary = "Cadastra um novo estudante universitário",
            description = "Cria uma conta de usuário vinculada com a role ESTUDANTE e registra as informações acadêmicas do estudante associando-o ao município de origem e à universidade de destino."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Estudante cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou conflito de unicidade (e-mail, CPF ou matrícula já existentes)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<StudentResponseDTO> registerStudent(@Valid @RequestBody StudentRegistrationRequestDTO request) {
        log.info("Student registration attempt for email: {}, CPF: {}", request.email(), request.cpf());
        StudentResponseDTO response = universityStudentService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
