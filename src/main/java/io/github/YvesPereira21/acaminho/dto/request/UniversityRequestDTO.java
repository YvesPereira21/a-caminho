package io.github.YvesPereira21.acaminho.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UniversityRequestDTO(
        @NotBlank String name,
        @NotBlank String cityName
) {}