package io.github.YvesPereira21.acaminho.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StateRequestDTO(
        @NotBlank String stateName
) {}