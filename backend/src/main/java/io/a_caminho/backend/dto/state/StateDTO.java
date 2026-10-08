package io.a_caminho.backend.dto.state;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para criação e representação de Estado")
public record StateDTO(
        @Schema(description = "Nome do estado", example = "Paraíba", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do estado é obrigatório")
        String stateName
) {}
