package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public record StateDTO(
        @NotBlank String stateName
) {}