package io.a_caminho.backend.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para criação e atualização de ônibus")
public record BusCreateDTO(
        @NotBlank(message = "O nome do ônibus é obrigatório")
        @Schema(description = "Identificação ou nome do ônibus", example = "Ônibus 01")
        String busName,

        @NotNull(message = "A quantidade de assentos é obrigatória")
        @Min(value = 1, message = "A quantidade de assentos deve ser no mínimo 1")
        @Schema(description = "Quantidade total de assentos", example = "44")
        Integer seatsQuantity
) {}
