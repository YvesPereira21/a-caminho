package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.CityService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Tag(name = "Cidades", description = "Endpoints de gerenciamento e consulta de Cidades")
public class CityController {

    private final CityService cityService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cadastra uma nova cidade",
            description = "Cadastra uma nova cidade vinculada a um estado existente. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cidade cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CityResponseDTO.class))
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
                    description = "Acesso negado: apenas administradores podem cadastrar cidades",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado informado não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<CityResponseDTO> createCity(@Valid @RequestBody CityRequestDTO cityRequestDTO) {
        log.info("Request to create city: {} in state: {}", cityRequestDTO.cityName(), cityRequestDTO.stateName());
        CityResponseDTO createdCity = cityService.createCity(cityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @Operation(
            summary = "Busca uma cidade pelo ID",
            description = "Recupera os detalhes de uma cidade através de seu identificador único UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cidade encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CityResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{cityId}")
    public ResponseEntity<CityResponseDTO> getCityById(
            @Parameter(description = "Identificador único da cidade", required = true)
            @PathVariable UUID cityId) {
        log.info("Request to fetch city by ID: {}", cityId);
        CityResponseDTO city = cityService.getCityById(cityId);
        return ResponseEntity.ok(city);
    }

    @Operation(
            summary = "Lista todas as cidades de um estado",
            description = "Retorna a listagem de todas as cidades pertencentes ao estado informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de cidades recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CityResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/state/{stateName}")
    public ResponseEntity<List<CityResponseDTO>> getAllCityFromStateByStateName(
            @Parameter(description = "Nome do estado para filtragem das cidades", required = true)
            @PathVariable String stateName) {
        log.info("Request to fetch all cities for state: {}", stateName);
        List<CityResponseDTO> cities = cityService.getAllCityFromStateByStateName(stateName);
        return ResponseEntity.ok(cities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Exclui uma cidade pelo ID",
            description = "Remove uma cidade cadastrada no sistema através de seu identificador único UUID. Operação restrita a administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Cidade excluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas administradores podem excluir cidades",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void> deleteCity(
            @Parameter(description = "Identificador único da cidade a ser excluída", required = true)
            @PathVariable UUID cityId) {
        log.info("Request to delete city by ID: {}", cityId);
        cityService.deleteCity(cityId);
        return ResponseEntity.noContent().build();
    }
}
