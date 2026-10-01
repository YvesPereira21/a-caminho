package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.UniversityStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Município ou Universidade informados não foram encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: e-mail, CPF ou matrícula já cadastrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<StudentDTO> registerStudent(@Valid @RequestBody StudentCreateDTO request) {
        log.info("Student registration attempt for email: {}, CPF: {}", request.email(), request.cpf());
        StudentDTO response = universityStudentService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Busca um estudante universitário pelo ID",
            description = "Recupera os dados de perfil e acadêmicos do estudante universitário correspondente ao ID informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do estudante recuperados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudent(
            @Parameter(description = "Identificador único do estudante universitário", required = true)
            @PathVariable UUID studentId) {
        log.info("Fetching student details for ID: {}", studentId);
        StudentDTO studentDTO = universityStudentService.getStudent(studentId);
        return ResponseEntity.ok(studentDTO);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Atualiza os dados cadastrais do estudante universitário",
            description = "Permite a atualização dos campos informados no DTO. Apenas o próprio estudante titular da conta possui permissão para atualizá-la."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Dados do estudante atualizados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o usuário autenticado não é o proprietário desta conta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante ou Universidade informados não encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PutMapping("/{studentId}")
    public ResponseEntity<Void> updateStudent(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Parameter(description = "Identificador único do estudante universitário", required = true)
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentUpdateDTO updateDTO) {
        log.info("Update student request for student ID: {} by user ID: {}", studentId, userId);
        universityStudentService.updateStudent(userId, studentId, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(
            summary = "Exclui a conta de um estudante universitário",
            description = "Remove a conta do estudante universitário informado. Apenas o próprio estudante proprietário ou um Administrador do sistema possuem permissão para realizar esta exclusão."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Conta do estudante removida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o usuário autenticado não tem permissão para excluir esta conta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudentAccount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Parameter(description = "Identificador único do estudante universitário", required = true)
            @PathVariable UUID studentId) {
        log.info("Delete student request for student ID: {} by user ID: {}", studentId, userId);
        universityStudentService.deleteStudentAccount(userId, studentId);
        return ResponseEntity.noContent().build();
    }
}
