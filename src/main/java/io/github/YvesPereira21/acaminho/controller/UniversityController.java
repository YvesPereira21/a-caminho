package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.UniversityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityResponseDTO;
import io.github.YvesPereira21.acaminho.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/universities")
@Validated
@Tag(name = "University", description = "Controller para gerenciamento da entidade University")
//@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping("")
    @Operation(summary = "Cria universidade", description = "Cria objeto universidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Universidade criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar universidade.")
    })
    public ResponseEntity<UniversityResponseDTO> createUniversity(@RequestBody UniversityRequestDTO universityRequestDTO){
        UniversityResponseDTO university =  universityService.createUniversity(universityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(university);
    }

    @GetMapping("/{universityName}")
    @Operation(summary = "Retorna lista de universidades", description = "Retorna lista de universidades com determinado nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Universidades retornadas com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao procurar universidades com determinado nome.")
    })
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversityContainingName(@PathVariable String universityName){
        return ResponseEntity.ok(universityService.allUniversityContainingName(universityName));
    }

    @DeleteMapping("/{universityId}")
    @Operation(summary = "Deleta objeto universidade", description = "Deleta objeto universidade baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Universidade deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Universidade não encontrada.")
    })
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID universityId){
        universityService.deleteUniversity(universityId);
        return ResponseEntity.noContent().build();
    }
}
