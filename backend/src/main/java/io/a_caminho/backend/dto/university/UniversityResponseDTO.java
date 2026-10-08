package io.a_caminho.backend.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta contendo os dados da universidade")
public record UniversityResponseDTO(
        @Schema(description = "Identificador único da universidade", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Nome da universidade", example = "Universidade Federal da Paraíba")
        String name,

        @Schema(description = "Campus da universidade", example = "Campus I")
        String campus,

        @Schema(description = "Nome da cidade da universidade", example = "João Pessoa")
        String cityName,

        @Schema(description = "Nome do estado da universidade", example = "Paraíba")
        String stateName
) {}
