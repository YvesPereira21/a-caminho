package io.a_caminho.backend.dto.municipality;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta contendo os dados do município")
public record MunicipalityResponseDTO(
        @Schema(description = "Identificador único do município", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID municipalityId,

        @Schema(description = "Nome do município", example = "Prefeitura de João Pessoa")
        String municipalityName
) {
    public MunicipalityResponseDTO(String municipalityName) {
        this(null, municipalityName);
    }
}
