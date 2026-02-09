package io.github.YvesPereira21.acaminho.dto;

import java.util.UUID;

public record BusLocationDTO(
        UUID busId,
        float latitude,
        float longitude
) {}
