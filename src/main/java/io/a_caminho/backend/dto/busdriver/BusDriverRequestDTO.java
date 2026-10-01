package io.a_caminho.backend.dto.busdriver;

import io.a_caminho.backend.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para criação de conta de motorista")
public record BusDriverRequestDTO(
        @Schema(description = "Nome completo do motorista", example = "Sebastião da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do motorista é obrigatório")
        String busDriverName,

        @Schema(description = "Dados do usuário do motorista", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Os dados do usuário são obrigatórios")
        @Valid
        UserDTO user
) {}
