package io.a_caminho.backend.dto.busdriver;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta contendo os dados do motorista")
public record BusDriverResponseDTO(
        @Schema(description = "Identificador único do motorista", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID busDriverId,

        @Schema(description = "Nome do motorista", example = "Sebastião da Silva")
        String busDriverName,

        @Schema(description = "E-mail cadastrado do motorista", example = "motorista@municipio.gov.br")
        String email
) {}
