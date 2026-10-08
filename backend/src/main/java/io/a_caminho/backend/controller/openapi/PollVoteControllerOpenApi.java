package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.pollvote.VoteRequestDTO;
import io.a_caminho.backend.dto.pollvote.VoteResponseDTO;
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

@Tag(name = "Votação em Enquetes", description = "Endpoints para registro, consulta e cancelamento de votos por estudantes universitários")
public interface PollVoteControllerOpenApi {

    @Operation(
            summary = "Registra um voto na enquete",
            description = "Permite que o estudante universitário autenticado registre seu ponto de embarque e confirme seu retorno para a rota do dia."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Voto registrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Enquete encerrada, usuário já votou ou ponto não pertence à rota",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas estudantes elegíveis podem votar",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enquete, estudante ou ponto de parada não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> vote(
            @Parameter(hidden = true) UUID studentUserId,
            @Parameter(description = "Identificador único da enquete", required = true) UUID pollId,
            VoteRequestDTO voteRequestDTO
    );

    @Operation(
            summary = "Consulta o próprio voto na enquete",
            description = "Recupera os dados do voto registrado pelo estudante universitário autenticado na enquete informada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Voto encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoteResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas estudantes podem consultar seus votos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Voto ainda não registrado para este estudante nesta enquete",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<VoteResponseDTO> getMyVote(
            @Parameter(hidden = true) UUID studentUserId,
            @Parameter(description = "Identificador único da enquete", required = true) UUID pollId
    );

    @Operation(
            summary = "Lista os passageiros confirmados na enquete",
            description = "Retorna a listagem nominal de estudantes que votaram na enquete informada, com seus respectivos pontos de parada e status de retorno. Operação restrita à prefeitura dona."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de passageiros recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PollPassengerDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: a enquete não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enquete não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<List<PollPassengerDTO>> getPollPassengers(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único da enquete", required = true) UUID pollId
    );

    @Operation(
            summary = "Cancela o voto na enquete",
            description = "Permite o cancelamento do voto antes do horário de encerramento da enquete."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Voto cancelado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Não é mais possível cancelar o voto após o encerramento da enquete",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas estudantes podem cancelar seus votos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Voto não encontrado para cancelamento",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> cancelVote(
            @Parameter(hidden = true) UUID studentUserId,
            @Parameter(description = "Identificador único da enquete", required = true) UUID pollId
    );
}
