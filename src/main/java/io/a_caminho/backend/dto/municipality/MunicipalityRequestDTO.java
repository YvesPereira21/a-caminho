package io.a_caminho.backend.dto.municipality;

import io.a_caminho.backend.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Requisição para criação de um novo município")
public record MunicipalityRequestDTO(
        @Schema(description = "Nome do município", example = "Prefeitura de João Pessoa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do município é obrigatório")
        String municipalityName,

        @Schema(description = "Dados do usuário da prefeitura", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Os dados do usuário são obrigatórios")
        @Valid
        UserDTO user,

        @Schema(description = "Identificador único da cidade associada", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID da cidade é obrigatório")
        UUID cityId
) {}
