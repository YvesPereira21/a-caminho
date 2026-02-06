package io.github.YvesPereira21.acaminho.dto.response;

import java.util.UUID;

public record BusDriverResponseDTO(
        UUID busDriverId,
        String busDriverName,
        String email,
        MunicipalityResponseDTO municipality
) {}
