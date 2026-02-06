package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record BusRequestDTO(
        @NotBlank String busName,
        @NotEmpty List<UUID> universityIds
) {}