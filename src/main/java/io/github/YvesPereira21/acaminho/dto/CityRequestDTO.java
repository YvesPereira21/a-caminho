package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public record CityRequestDTO(
        @NotBlank String cityName,
        @NotBlank String stateName
) {}