package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.UniversityStudentRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityStudentResponseDTO;
import io.github.YvesPereira21.acaminho.service.UniversityStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/universityStudents")
@Validated
@Tag(name = "University Student", description = "Controller para gerenciamento da entidade University Student")
//@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class UniversityStudentController {

    private final UniversityStudentService universityStudentService;

    public UniversityStudentController(UniversityStudentService universityStudentService) {
        this.universityStudentService = universityStudentService;
    }

    @PostMapping("")
    @Operation(summary = "Cria estudante universitário", description = "Cria conta de estudante universitário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Universidade criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar universidade.")
    })
    public ResponseEntity<UniversityStudentResponseDTO> createUniversityStudent(@RequestBody UniversityStudentRequestDTO universityStudentRequestDTO) {
        UniversityStudentResponseDTO university = universityStudentService.createUniversityStudentAccount(universityStudentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(university);
    }

    @GetMapping("/{universityStudentId}")
    @Operation(summary = "Retorna objeto estudante universitário", description = "Retorna objeto estudante universitário baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudante universitário retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Estudante universitário não encontrado.")
    })
    public ResponseEntity<UniversityStudentResponseDTO> getUniversityStudentById(@PathVariable UUID universityStudentId) {
        return ResponseEntity.ok(universityStudentService.getUniversityStudentById(universityStudentId));
    }

    @DeleteMapping("/{universityStudentId}")
    @Operation(summary = "Deleta conta do estudante universitário", description = "Deleta conta do estudante universitário baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estudante universitário deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Estudante universitário não encontrado.")
    })
    public ResponseEntity<Void> deleteUniversityStudentById(@PathVariable UUID universityStudentId) {
        universityStudentService.deleteUniversityStudentById(universityStudentId);
        return ResponseEntity.noContent().build();
    }
}
