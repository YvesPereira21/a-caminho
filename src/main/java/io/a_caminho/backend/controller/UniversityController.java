package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
@Tag(name = "Universidades", description = "Endpoints de gerenciamento e consulta de Universidades")
public class UniversityController {

    private final UniversityService universityService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cadastra uma nova universidade",
            description = "Cadastra uma nova universidade vinculada a uma cidade e estado existentes. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Universidade cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UniversityResponseDTO.class))
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
                    description = "Acesso negado: apenas administradores podem cadastrar universidades",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade informada não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<UniversityResponseDTO> createUniversity(
            @Valid @RequestBody UniversityRequestDTO requestDTO) {
        log.info("Request to create university: {} in {}, {}", requestDTO.name(), requestDTO.cityName(), requestDTO.stateName());
        UniversityResponseDTO createdUniversity = universityService.createUniversity(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUniversity);
    }

    @Operation(
            summary = "Lista universidades por nome e estado",
            description = "Retorna a listagem de universidades que correspondam ao nome/campus informado dentro do estado especificado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de universidades recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UniversityResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversityByNameFromState(
            @Parameter(description = "Nome ou termo para filtro da universidade", required = true)
            @RequestParam String universityName,
            @Parameter(description = "Nome do estado para filtragem das universidades", required = true)
            @RequestParam String stateName) {
        log.info("Request to fetch universities by name: {} and state: {}", universityName, stateName);
        List<UniversityResponseDTO> universities = universityService.getAllUniversityByNameFromState(universityName, stateName);
        return ResponseEntity.ok(universities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Exclui uma universidade pelo ID",
            description = "Remove uma universidade cadastrada no sistema através de seu identificador único UUID. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Universidade excluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas administradores podem excluir universidades",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Universidade não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{universityId}")
    public ResponseEntity<Void> deleteUniversity(
            @Parameter(description = "Identificador único da universidade a ser excluída", required = true)
            @PathVariable UUID universityId) {
        log.info("Request to delete university with ID: {}", universityId);
        universityService.deleteUniversity(universityId);
        return ResponseEntity.noContent().build();
    }
}
