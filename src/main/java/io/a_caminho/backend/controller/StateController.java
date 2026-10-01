package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.StateService;
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

@Slf4j
@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
@Tag(name = "Estados", description = "Endpoints de gerenciamento de Estados")
public class StateController {

    private final StateService stateService;

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
    @GetMapping
    public ResponseEntity<List<StateDTO>> getAllState() {
        log.info("Request to fetch all states");
        List<StateDTO> states = stateService.getAllState();
        return ResponseEntity.ok(states);
    }

    @PreAuthorize("hasRole('ADMIN')")
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
    @PostMapping
    public ResponseEntity<StateDTO> createState(@Valid @RequestBody StateDTO stateDTO) {
        log.info("Request to create state: {}", stateDTO.stateName());
        StateDTO createdState = stateService.createState(stateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdState);
    }

    @PreAuthorize("hasRole('ADMIN')")
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
    @DeleteMapping("/{stateName}")
    public ResponseEntity<Void> deleteState(
            @Parameter(description = "Nome do estado a ser excluído", required = true)
            @PathVariable String stateName) {
        log.info("Request to delete state: {}", stateName);
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}
