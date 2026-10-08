package io.a_caminho.backend.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para cadastro de uma universidade")
public record UniversityRequestDTO(
        @Schema(description = "Nome da universidade", example = "Universidade Federal da Paraíba", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome da universidade é obrigatório")
        String name,

        @Schema(description = "Campus da universidade", example = "Campus I")
        String campus,

        @Schema(description = "Nome da cidade onde a universidade está localizada", example = "João Pessoa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome da cidade é obrigatório")
        String cityName,

        @Schema(description = "Nome do estado onde a universidade está localizada", example = "Paraíba", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do estado é obrigatório")
        String stateName
) {}
