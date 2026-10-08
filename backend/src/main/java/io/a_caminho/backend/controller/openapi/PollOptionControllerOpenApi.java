package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
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

@Tag(name = "Pontos de Ônibus (Opções de Enquete)", description = "Endpoints de gerenciamento de pontos de embarque e desembarque para enquetes")
public interface PollOptionControllerOpenApi {

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
    ResponseEntity<PollOptionResponseDTO> createPollOption(
            @Parameter(hidden = true) UUID municipalityUserId,
            PollOptionDTO pollOptionDTO
    );

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
    ResponseEntity<List<PollOptionResponseDTO>> getAllPollOptions(
            @Parameter(hidden = true) UUID municipalityUserId
    );

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
    ResponseEntity<PollOptionResponseDTO> getPollOption(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true) UUID optionId
    );

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
    ResponseEntity<Void> updatePollOption(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true) UUID optionId,
            PollOptionDTO pollOptionDTO
    );

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
    ResponseEntity<Void> deletePollOption(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do ponto de ônibus", required = true) UUID optionId
    );
}
