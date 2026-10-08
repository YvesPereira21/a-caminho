package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Enquetes", description = "Endpoints para consulta e gerenciamento de enquetes de transporte universitário")
public interface PollControllerOpenApi {

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
    ResponseEntity<PollDTO> getPoll(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "Identificador único da enquete", required = true) UUID pollId
    );

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
    ResponseEntity<List<PollListDTO>> getAllPollsFromMunicipality(
            @Parameter(hidden = true) UUID municipalityUserId
    );

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
    ResponseEntity<List<PollListDTO>> getOpenPollsToStudent(
            @Parameter(hidden = true) UUID studentUserId
    );
}
