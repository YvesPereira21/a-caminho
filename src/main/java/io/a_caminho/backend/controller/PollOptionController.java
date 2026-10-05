package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.PollOptionService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/poll-options")
@RequiredArgsConstructor
@Tag(name = "Pontos de Ônibus (Opções de Enquete)", description = "Endpoints de gerenciamento de pontos de embarque e desembarque para enquetes")
public class PollOptionController {

    private final PollOptionService pollOptionService;

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Cadastra um novo ponto de ônibus",
            description = "Cadastra um novo ponto de parada associado à prefeitura autenticada. Operação restrita a prefeituras."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ponto de ônibus cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollOptionResponseDTO.class))
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
                    description = "Acesso negado: apenas prefeituras podem cadastrar pontos de ônibus",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um ponto de ônibus com este nome no município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<PollOptionResponseDTO> createPollOption(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody PollOptionDTO pollOptionDTO
    ) {
        log.info("Request to create poll option '{}' by municipality user ID: {}", pollOptionDTO.stopName(), municipalityUserId);
        PollOptionResponseDTO created = pollOptionService.createPollOption(municipalityUserId, pollOptionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Lista todos os pontos de ônibus da prefeitura",
            description = "Retorna a listagem de todos os pontos de parada cadastrados para a prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pontos de ônibus recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PollOptionResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem consultar seus pontos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<List<PollOptionResponseDTO>> getAllPollOptions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all poll options for municipality user ID: {}", municipalityUserId);
        List<PollOptionResponseDTO> options = pollOptionService.getAllPollOptions(municipalityUserId);
        return ResponseEntity.ok(options);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Busca um ponto de ônibus por ID",
            description = "Recupera os dados de um ponto de parada específico pertencente à prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ponto de ônibus encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollOptionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o ponto não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ponto de ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{optionId}")
    public ResponseEntity<PollOptionResponseDTO> getPollOption(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true)
            @PathVariable UUID optionId
    ) {
        log.info("Request to fetch poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        PollOptionResponseDTO option = pollOptionService.getPollOption(municipalityUserId, optionId);
        return ResponseEntity.ok(option);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Atualiza um ponto de ônibus",
            description = "Atualiza os dados de um ponto de parada existente pertencente à prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Ponto de ônibus atualizado com sucesso"
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
                    description = "Acesso negado: o ponto não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ponto de ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe outro ponto com este nome no município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PutMapping("/{optionId}")
    public ResponseEntity<Void> updatePollOption(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true)
            @PathVariable UUID optionId,
            @Valid @RequestBody PollOptionDTO pollOptionDTO
    ) {
        log.info("Request to update poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        pollOptionService.updatePollOption(municipalityUserId, optionId, pollOptionDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Exclui um ponto de ônibus",
            description = "Remove um ponto de parada pertencente à prefeitura autenticada. A exclusão será rejeitada caso existam votos vinculados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Ponto de ônibus excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Não é possível excluir o ponto pois existem votos vinculados a ele",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o ponto não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ponto de ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deletePollOption(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true)
            @PathVariable UUID optionId
    ) {
        log.info("Request to delete poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        pollOptionService.deleteOption(municipalityUserId, optionId);
        return ResponseEntity.noContent().build();
    }
}
