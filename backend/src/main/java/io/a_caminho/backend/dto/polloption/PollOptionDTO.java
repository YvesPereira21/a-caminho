package io.a_caminho.backend.dto.polloption;

import jakarta.validation.constraints.NotBlank;

public record PollOptionDTO(
        @NotBlank String stopName,
        Double latitude,
        Double longitude
) {
    public PollOptionDTO(String stopName) {
        this(stopName, null, null);
    }
}
