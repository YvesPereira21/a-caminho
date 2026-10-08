package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.state.StateDTO;
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

@Tag(name = "Estados", description = "Endpoints de gerenciamento de Estados")
public interface StateControllerOpenApi {

    @Operation(
            summary = "Lista todos os estados",
            description = "Retorna a listagem de todos os estados cadastrados no sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de estados recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StateDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<List<StateDTO>> getAllState();

    @Operation(
            summary = "Cadastra um novo estado",
            description = "Cadastra um novo estado no sistema. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Estado cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StateDTO.class))
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
                    description = "Acesso negado: apenas administradores podem cadastrar estados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: estado com esse nome já cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<StateDTO> createState(StateDTO stateDTO);

    @Operation(
            summary = "Exclui um estado pelo nome",
            description = "Remove um estado cadastrado no sistema pelo seu nome. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Estado excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas administradores podem excluir estados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> deleteState(
            @Parameter(description = "Nome do estado a ser excluído", required = true) String stateName
    );
}
