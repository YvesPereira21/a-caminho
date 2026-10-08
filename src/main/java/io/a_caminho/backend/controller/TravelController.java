package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.travel.TravelCancelDTO;
import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.service.TravelService;
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
@RequestMapping("/api/travels")
@RequiredArgsConstructor
@Tag(name = "Viagens", description = "Endpoints de gerenciamento e operação de viagens de transporte universitário")
public class TravelController {

    private final TravelService travelService;

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Inicia uma nova viagem (trecho de ida)",
            description = "Cria e inicia uma viagem no sentido IDA (OUTBOUND) vinculada à enquete e ao ônibus informados. Operação restrita ao perfil MOTORISTA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Viagem iniciada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou violação de regra de negócio (motorista ou ônibus já em viagem ativa)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: recursos pertencem a município diferente do motorista ou perfil sem permissão",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enquete, ônibus ou motorista não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PostMapping
    public ResponseEntity<TravelDTO> createTravel(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId,
            @Valid @RequestBody TravelCreateDTO travelDTO
    ) {
        log.info("Request to create travel by bus driver user ID: {}", busDriverUserId);
        TravelDTO createdTravel = travelService.createTravel(busDriverUserId, travelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTravel);
    }

    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @Operation(
            summary = "Busca uma viagem por ID",
            description = "Recupera os detalhes de uma viagem específica. Acesso restrito a motoristas ou prefeituras do mesmo município."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: a viagem não pertence ao seu município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{travelId}")
    public ResponseEntity<TravelDTO> getTravelById(
            @Parameter(description = "Identificador único da viagem", required = true)
            @PathVariable UUID travelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        log.info("Request to fetch travel ID: {} by user ID: {}", travelId, userId);
        TravelDTO travel = travelService.getTravelById(travelId, userId);
        return ResponseEntity.ok(travel);
    }

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Busca a viagem atualmente em andamento do motorista",
            description = "Recupera os dados da viagem com status EM_ANDAMENTO atribuída ao motorista autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem em andamento encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
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
                    description = "Nenhuma viagem em andamento encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/active")
    public ResponseEntity<TravelDTO> getActiveTravelByDriver(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch active travel for bus driver user ID: {}", busDriverUserId);
        TravelDTO activeTravel = travelService.getActiveTravelByDriver(busDriverUserId);
        return ResponseEntity.ok(activeTravel);
    }

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Lista o histórico de viagens do motorista",
            description = "Retorna todas as viagens realizadas pelo motorista autenticado, ordenadas da mais recente para a mais antiga."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Histórico de viagens recuperado com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TravelDTO.class)))
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
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/driver")
    public ResponseEntity<List<TravelDTO>> getAllTravelsByDriver(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch all travels for bus driver user ID: {}", busDriverUserId);
        List<TravelDTO> travels = travelService.getAllTravelsByDriver(busDriverUserId);
        return ResponseEntity.ok(travels);
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @Operation(
            summary = "Lista todas as viagens da prefeitura",
            description = "Retorna o histórico e as viagens ativas pertencentes à prefeitura autenticada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de viagens da prefeitura recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TravelDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: apenas prefeituras podem acessar este recurso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/municipality")
    public ResponseEntity<List<TravelDTO>> getAllTravelsByMunicipality(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all travels for municipality user ID: {}", municipalityUserId);
        List<TravelDTO> travels = travelService.getAllTravelsByMunicipality(municipalityUserId);
        return ResponseEntity.ok(travels);
    }

    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @Operation(
            summary = "Lista os passageiros da viagem",
            description = "Retorna a lista de passageiros confirmados. Se a viagem estiver em IDA (OUTBOUND), lista todos os confirmados; se em VOLTA (INBOUND), filtra apenas os que confirmaram o retorno."
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
                    description = "Acesso negado: a viagem não pertence ao seu município",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem ou enquete não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @GetMapping("/{travelId}/passengers")
    public ResponseEntity<List<PollPassengerDTO>> getTravelPassengers(
            @Parameter(description = "Identificador único da viagem", required = true)
            @PathVariable UUID travelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        log.info("Request to fetch passengers for travel ID: {} by user ID: {}", travelId, userId);
        List<PollPassengerDTO> passengers = travelService.getTravelPassengers(travelId, userId);
        return ResponseEntity.ok(passengers);
    }

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Inicia a viagem de retorno (trecho de volta)",
            description = "Altera o sentido da viagem em andamento para VOLTA (INBOUND). Apenas o motorista condutor da viagem pode executar esta ação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem de retorno iniciada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A viagem não está em andamento ou a volta já foi iniciada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: você não é o motorista responsável por esta viagem",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PatchMapping("/{travelId}/start-return")
    public ResponseEntity<TravelDTO> startReturn(
            @Parameter(description = "Identificador único da viagem", required = true)
            @PathVariable UUID travelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to start return for travel ID: {} by driver user ID: {}", travelId, busDriverUserId);
        TravelDTO updatedTravel = travelService.startReturn(travelId, busDriverUserId);
        return ResponseEntity.ok(updatedTravel);
    }

    @PreAuthorize("hasRole('BUS_DRIVER')")
    @Operation(
            summary = "Conclui a viagem",
            description = "Finaliza a viagem em andamento, alterando seu status para CONCLUIDA (COMPLETED). Apenas o motorista condutor pode concluir a viagem."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem concluída com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A viagem não está em andamento",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: você não é o motorista responsável por esta viagem",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PatchMapping("/{travelId}/complete")
    public ResponseEntity<TravelDTO> completeTravel(
            @Parameter(description = "Identificador único da viagem", required = true)
            @PathVariable UUID travelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to complete travel ID: {} by driver user ID: {}", travelId, busDriverUserId);
        TravelDTO updatedTravel = travelService.completeTravel(travelId, busDriverUserId);
        return ResponseEntity.ok(updatedTravel);
    }

    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @Operation(
            summary = "Cancela uma viagem",
            description = "Cancela uma viagem não finalizada, registrando o motivo do cancelamento. Operação permitida para o motorista responsável ou prefeitura."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem cancelada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TravelDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Viagem já concluída ou cancelada, ou dados inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: usuário sem permissão para cancelar esta viagem",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    @PatchMapping("/{travelId}/cancel")
    public ResponseEntity<TravelDTO> cancelTravel(
            @Parameter(description = "Identificador único da viagem", required = true)
            @PathVariable UUID travelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody TravelCancelDTO cancelDTO
    ) {
        log.info("Request to cancel travel ID: {} by user ID: {}", travelId, userId);
        TravelDTO updatedTravel = travelService.cancelTravel(travelId, userId, cancelDTO.cancellationReason());
        return ResponseEntity.ok(updatedTravel);
    }
}
