package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@Tag(name = "Enquetes", description = "Endpoints para consulta e gerenciamento de enquetes de transporte universitário")
public class PollController {

    private final PollService pollService;

    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'STUDENT')")
    @Operation(
            summary = "Consulta detalhes e opções da enquete",
            description = "Recupera os detalhes completos da enquete com suas opções de pontos de parada. Acesso permitido à prefeitura dona e aos estudantes elegíveis da rota."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalhes da enquete recuperados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estudante não pertence à universidade atendida por esta rota",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: prefeitura não proprietária ou estudante de outro município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enquete ou perfil de usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDTO> getPoll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Parameter(description = "Identificador único da enquete", required = true)
            @PathVariable UUID pollId
    ) {
        log.info("Request to fetch poll ID: {} by user ID: {}", pollId, userId);
        PollDTO pollDTO = pollService.getPoll(userId, pollId);
        return ResponseEntity.ok(pollDTO);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Lista todas as enquetes da prefeitura",
            description = "Retorna a listagem de todas as enquetes criadas para a prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de enquetes recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PollListDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem consultar esta listagem",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<List<PollListDTO>> getAllPollsFromMunicipality(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all polls for municipality user ID: {}", municipalityUserId);
        List<PollListDTO> polls = pollService.getAllPollFromMunicipality(municipalityUserId);
        return ResponseEntity.ok(polls);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Lista enquetes abertas para o estudante",
            description = "Retorna as enquetes abertas para votação no momento presente para a cidade e universidade do estudante autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de enquetes abertas recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PollListDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas estudantes podem consultar enquetes abertas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Perfil de estudante não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/open")
    public ResponseEntity<List<PollListDTO>> getOpenPollsToStudent(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID studentUserId
    ) {
        log.info("Request to fetch open polls for student user ID: {}", studentUserId);
        List<PollListDTO> polls = pollService.getOpenPollsToStudent(studentUserId);
        return ResponseEntity.ok(polls);
    }
}
