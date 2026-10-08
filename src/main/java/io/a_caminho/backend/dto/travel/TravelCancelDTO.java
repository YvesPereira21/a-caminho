package io.a_caminho.backend.dto.travel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para cancelamento de viagem")
public record TravelCancelDTO(
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        @Schema(description = "Motivo do cancelamento da viagem", example = "Problema mecânico no ônibus")
        String cancellationReason
) {}
