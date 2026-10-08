package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
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

@Tag(name = "Motoristas de Ônibus", description = "Endpoints de gerenciamento de Motoristas vinculados a Prefeituras")
public interface BusDriverControllerOpenApi {

    @Operation(
            summary = "Cadastra um novo motorista de ônibus",
            description = "Cria uma conta de motorista com perfil MOTORISTA e associa-a à prefeitura do usuário autenticado. Operação restrita ao perfil PREFEITURA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Motorista cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusDriverResponseDTO.class))
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
                    description = "Acesso negado: apenas prefeituras podem cadastrar motoristas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prefeitura não encontrada para o usuário autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: e-mail já cadastrado no sistema",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<BusDriverResponseDTO> createBusDriver(
            @Parameter(hidden = true) UUID municipalityUserId,
            BusDriverRequestDTO requestDTO
    );

    @Operation(
            summary = "Busca um motorista de ônibus pelo ID",
            description = "Recupera os detalhes de um motorista através de seu identificador único. Apenas a prefeitura à qual o motorista pertence pode consultá-lo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Motorista encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusDriverResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o motorista não pertence à prefeitura autenticada ou perfil sem permissão",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<BusDriverResponseDTO> getBusDriverById(
            @Parameter(description = "Identificador único do motorista de ônibus", required = true) UUID busDriverId,
            @Parameter(hidden = true) UUID municipalityUserId
    );

    @Operation(
            summary = "Lista todos os motoristas da prefeitura autenticada",
            description = "Retorna todos os motoristas de ônibus vinculados à prefeitura do usuário autenticado. Operação restrita ao perfil PREFEITURA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de motoristas recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BusDriverResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem listar seus motoristas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<List<BusDriverResponseDTO>> getAllBusDriversFromMunicipality(
            @Parameter(hidden = true) UUID municipalityUserId
    );

    @Operation(
            summary = "Exclui um motorista de ônibus pelo ID",
            description = "Remove o cadastro de um motorista de ônibus. Apenas a prefeitura à qual o motorista pertence pode excluí-lo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Motorista excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o motorista não pertence à prefeitura autenticada ou perfil sem permissão",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> deleteBusDriverAccount(
            @Parameter(description = "Identificador único do motorista de ônibus a ser excluído", required = true) UUID busDriverId,
            @Parameter(hidden = true) UUID municipalityUserId
    );
}
