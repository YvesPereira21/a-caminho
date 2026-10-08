package io.a_caminho.backend.dto.travel;

import io.a_caminho.backend.model.enums.TravelStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record TravelCreateDTO(
        @NotNull UUID pollId,
        @NotNull UUID busId,
        @NotNull TravelStatus status,
        @NotNull LocalTime returnTime
) {}
