package io.a_caminho.backend.dto.city;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para criação de uma nova cidade")
public record CityRequestDTO(
        @Schema(description = "Nome da cidade", example = "João Pessoa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome da cidade é obrigatório")
        String cityName,

        @Schema(description = "Nome do estado ao qual a cidade pertence", example = "Paraíba", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do estado é obrigatório")
        String stateName
) {}
