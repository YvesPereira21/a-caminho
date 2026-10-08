package io.a_caminho.backend.dto.polloption;

import jakarta.validation.constraints.NotBlank;

public record PollOptionDTO(
        @NotBlank String stopName
) {}
