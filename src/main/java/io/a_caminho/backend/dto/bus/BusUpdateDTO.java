package io.a_caminho.backend.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Requisição para atualização de ônibus")
public record BusUpdateDTO(
        @Schema(description = "Identificação ou nome do ônibus", example = "Ônibus 01")
        String busName,

        @Min(value = 1, message = "A quantidade de assentos deve ser no mínimo 1")
        @Schema(description = "Quantidade total de assentos", example = "44")
        Integer seatsQuantity
) {}
