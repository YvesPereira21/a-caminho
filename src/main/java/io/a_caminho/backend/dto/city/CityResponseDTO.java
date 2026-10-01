package io.a_caminho.backend.dto.city;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta contendo os dados da cidade")
public record CityResponseDTO(
        @Schema(description = "Identificador único da cidade", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID cityId,

        @Schema(description = "Nome da cidade", example = "João Pessoa")
        String cityName,

        @Schema(description = "Nome do estado ao qual a cidade pertence", example = "Paraíba")
        String stateName
) {}
