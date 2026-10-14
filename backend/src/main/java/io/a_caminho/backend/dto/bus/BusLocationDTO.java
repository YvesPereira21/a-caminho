package io.a_caminho.backend.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de localização geográfica do ônibus em tempo real")
public record BusLocationDTO(
        @Schema(description = "Identificador único do ônibus", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "O ID do ônibus é obrigatório")
        UUID busId,

        @Schema(description = "Coordenada de latitude", example = "-6.8600")
        float latitude,

        @Schema(description = "Coordenada de longitude", example = "-35.4870")
        float longitude,

        @Schema(description = "Timestamp do registro de localização", example = "2026-10-13T10:30:00")
        LocalDateTime timestamp
) {
    public BusLocationDTO(UUID busId, float latitude, float longitude) {
        this(busId, latitude, longitude, LocalDateTime.now());
    }
}

