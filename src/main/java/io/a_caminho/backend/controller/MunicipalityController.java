package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.MunicipalityService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
@Tag(name = "Prefeituras", description = "Endpoints de gerenciamento e consulta de Prefeituras")
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cadastra uma nova prefeitura",
            description = "Cadastra uma nova prefeitura vinculando-a a uma cidade existente e criando a conta de usuário associada com o perfil PREFEITURA. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Prefeitura cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MunicipalityResponseDTO.class))
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
                    description = "Acesso negado: apenas administradores podem cadastrar prefeituras",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade informada não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: e-mail já cadastrado no sistema",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<MunicipalityResponseDTO> createMunicipality(
            @Valid @RequestBody MunicipalityRequestDTO municipalityRequestDTO) {
        log.info("Request to create municipality: {}", municipalityRequestDTO.municipalityName());
        MunicipalityResponseDTO createdMunicipality = municipalityService.createMunicipality(municipalityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMunicipality);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Busca uma prefeitura pelo nome",
            description = "Recupera os dados de uma prefeitura cadastrada através de seu nome. Operação restrita ao perfil PREFEITURA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Prefeitura encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MunicipalityResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem consultar prefeitura por nome",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prefeitura não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{municipalityName}")
    public ResponseEntity<MunicipalityResponseDTO> getMunicipalityByName(
            @Parameter(description = "Nome da prefeitura a ser consultada", required = true)
            @PathVariable String municipalityName) {
        log.info("Request to fetch municipality by name: {}", municipalityName);
        MunicipalityResponseDTO municipality = municipalityService.getMunicipalityByName(municipalityName);
        return ResponseEntity.ok(municipality);
    }

    @Operation(
            summary = "Lista todas as prefeituras de um estado",
            description = "Retorna a listagem de todas as prefeituras cadastradas que pertencem ao estado informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de prefeituras recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MunicipalityResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/state/{stateName}")
    public ResponseEntity<List<MunicipalityResponseDTO>> getAllMunicipalityFromState(
            @Parameter(description = "Nome do estado para filtragem das prefeituras", required = true)
            @PathVariable String stateName) {
        log.info("Request to fetch all municipalities for state: {}", stateName);
        List<MunicipalityResponseDTO> municipalities = municipalityService.getAllMunicipalityFromState(stateName);
        return ResponseEntity.ok(municipalities);
    }

    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'ADMIN')")
    @Operation(
            summary = "Exclui uma prefeitura pelo nome",
            description = "Remove o cadastro de uma prefeitura do sistema através de seu nome. Operação restrita aos perfis PREFEITURA e ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Prefeitura excluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras e administradores podem excluir prefeituras",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prefeitura não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{municipalityName}")
    public ResponseEntity<Void> deleteMunicipality(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Parameter(description = "Nome da prefeitura a ser excluída", required = true)
            @PathVariable String municipalityName) {
        log.info("Request to delete municipality: {} by user ID: {}", municipalityName, municipalityUserId);
        municipalityService.deleteMunicipality(municipalityUserId, municipalityName);
        return ResponseEntity.noContent().build();
    }
}
