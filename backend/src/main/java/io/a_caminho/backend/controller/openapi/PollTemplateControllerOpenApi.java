package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
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

@Tag(name = "Modelos de Enquete (Rotas Recorrentes)", description = "Endpoints de gerenciamento de templates para criação automática de enquetes diárias")
public interface PollTemplateControllerOpenApi {

    @Operation(
            summary = "Cadastra um novo modelo de enquete",
            description = "Cadastra uma rota de enquete recorrente associada à prefeitura autenticada. Operação restrita a prefeituras."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Modelo de enquete cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollTemplateDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição ou horários inconsistentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem cadastrar modelos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Universidade ou ponto de parada informado não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um modelo de enquete com esta rota e turno no município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<PollTemplateDTO> createPollTemplate(
            @Parameter(hidden = true) UUID municipalityUserId,
            PollTemplateCreateDTO pollTemplateCreateDTO
    );

    @Operation(
            summary = "Lista todos os modelos de enquete da prefeitura",
            description = "Retorna a listagem de todos os modelos de rota cadastrados para a prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de modelos recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PollTemplateDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem consultar seus modelos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<List<PollTemplateDTO>> getAllPollTemplates(
            @Parameter(hidden = true) UUID municipalityUserId
    );

    @Operation(
            summary = "Busca um modelo de enquete por ID",
            description = "Recupera os dados de um modelo de rota específico pertencente à prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modelo de enquete encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollTemplateDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o modelo não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Modelo de enquete não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<PollTemplateDTO> getPollTemplate(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do modelo de enquete", required = true) UUID templateId
    );

    @Operation(
            summary = "Desativa um modelo de enquete",
            description = "Marca um modelo de rota como inativo para suspender a geração automática de enquetes diárias."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Modelo de enquete desativado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o modelo não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Modelo de enquete não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> deactivatePollTemplate(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do modelo de enquete", required = true) UUID templateId
    );

    @Operation(
            summary = "Exclui um modelo de enquete",
            description = "Remove um modelo de rota pertencente à prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Modelo de enquete excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o modelo não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Modelo de enquete não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> deletePollTemplate(
            @Parameter(hidden = true) UUID municipalityUserId,
            @Parameter(description = "Identificador único do modelo de enquete", required = true) UUID templateId
    );
}
