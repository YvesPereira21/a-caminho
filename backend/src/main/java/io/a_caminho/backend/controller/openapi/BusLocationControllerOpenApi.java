package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.bus.BusLocationDTO;
import io.a_caminho.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Bus Locations", description = "Endpoints para consulta de localização geográfica dos ônibus em tempo real")
public interface BusLocationControllerOpenApi {

    @Operation(
            summary = "Consulta a última localização conhecida de um ônibus",
            description = "Recupera as coordenadas mais recentes emitidas pelo ônibus para carga inicial do mapa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Última localização retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma coordenada emitida recentemente para o ônibus informado")
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<BusLocationDTO> getLatestLocation(
            @Parameter(description = "Identificador único do ônibus", required = true) UUID busId
    );

    @Operation(
            summary = "Lista as localizações dos ônibus ativos da prefeitura",
            description = "Retorna o mapa de coordenadas atuais dos ônibus em trânsito pertencentes à frota da prefeitura autenticada (ou todos se administrador)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapa de localizações retornado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas prefeituras ou administradores podem consultar a frota ativa")
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Map<UUID, BusLocationDTO>> getAllLocations(
            @Parameter(hidden = true) UserPrincipal currentUser
    );
}

