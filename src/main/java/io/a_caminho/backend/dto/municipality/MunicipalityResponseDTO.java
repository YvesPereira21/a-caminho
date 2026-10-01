package io.a_caminho.backend.dto.municipality;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta contendo os dados do município")
public record MunicipalityResponseDTO(
        @Schema(description = "Nome do município", example = "Prefeitura de João Pessoa")
        String municipalityName
) {}
