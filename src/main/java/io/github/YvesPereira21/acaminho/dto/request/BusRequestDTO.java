package io.github.YvesPereira21.acaminho.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BusRequestDTO(
        @NotBlank String busName,
        @NotNull UUID busDriverId,
        @NotEmpty List<UUID> universityIds
) {}