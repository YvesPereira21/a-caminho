package io.github.YvesPereira21.acaminho.dto;

import java.util.UUID;

public record WarningMessageDTO(
        String message,
        UUID municipalityId,
        UUID busId
) {}
