package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.BusService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Tag(name = "Ônibus", description = "Endpoints de gerenciamento da frota de ônibus")
public class BusController {

    private final BusService busService;

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Cadastra um novo ônibus",
            description = "Cadastra um novo ônibus vinculado à prefeitura autenticada. Operação restrita ao perfil PREFEITURA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ônibus cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusDTO.class))
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
                    description = "Acesso negado: apenas prefeituras podem cadastrar ônibus",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prefeitura não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: já existe um ônibus com este nome cadastrado no município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<BusDTO> createBus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody BusCreateDTO busCreateDTO
    ) {
        log.info("Request to create bus '{}' for municipality user ID: {}", busCreateDTO.busName(), municipalityUserId);
        BusDTO createdBus = busService.createBus(municipalityUserId, busCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBus);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Busca um ônibus por ID",
            description = "Recupera os detalhes de um ônibus pelo seu identificador único. Apenas a prefeitura proprietária pode consultá-lo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ônibus encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o ônibus não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{busId}")
    public ResponseEntity<BusDTO> getBusById(
            @Parameter(description = "Identificador único do ônibus", required = true)
            @PathVariable UUID busId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        BusDTO bus = busService.getBusById(municipalityUserId, busId);
        return ResponseEntity.ok(bus);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Lista todos os ônibus da prefeitura",
            description = "Retorna a listagem de todos os ônibus pertencentes à frota da prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ônibus recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BusDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem listar seus ônibus",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<List<BusDTO>> getAllBusesByMunicipality(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all buses for municipality user ID: {}", municipalityUserId);
        List<BusDTO> buses = busService.getAllBusesByMunicipality(municipalityUserId);
        return ResponseEntity.ok(buses);
    }

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Lista todos os ônibus disponíveis para o motorista",
            description = "Retorna a frota de ônibus da prefeitura à qual o motorista autenticado está vinculado. Utilizado na seleção de veículo para viagem."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ônibus recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BusDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas motoristas podem acessar este recurso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motorista ou prefeitura não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/driver")
    public ResponseEntity<List<BusDTO>> getAllBusesForDriver(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch all buses for bus driver user ID: {}", busDriverUserId);
        List<BusDTO> buses = busService.getAllBusesForDriver(busDriverUserId);
        return ResponseEntity.ok(buses);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Atualiza os dados de um ônibus",
            description = "Atualiza as informações de um ônibus existente. Apenas a prefeitura proprietária pode alterá-lo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ônibus atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusDTO.class))
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
                    description = "Acesso negado: o ônibus não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: já existe outro ônibus com este nome no município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PutMapping("/{busId}")
    public ResponseEntity<BusDTO> updateBus(
            @Parameter(description = "Identificador único do ônibus", required = true)
            @PathVariable UUID busId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody BusUpdateDTO busUpdateDTO
    ) {
        log.info("Request to update bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        BusDTO updatedBus = busService.updateBus(municipalityUserId, busId, busUpdateDTO);
        return ResponseEntity.ok(updatedBus);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Exclui um ônibus da frota",
            description = "Exclui um ônibus cadastrado. Não é permitido excluir ônibus que possuam viagens vinculadas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Ônibus excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Violação de integridade: existem viagens associadas a este veículo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o ônibus não pertence à prefeitura autenticada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @DeleteMapping("/{busId}")
    public ResponseEntity<Void> deleteBus(
            @Parameter(description = "Identificador único do ônibus a ser excluído", required = true)
            @PathVariable UUID busId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to delete bus ID: {} by municipality user ID: {}", busId, municipalityUserId);
        busService.deleteBus(municipalityUserId, busId);
        return ResponseEntity.noContent().build();
    }
}
