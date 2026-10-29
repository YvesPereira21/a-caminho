package io.a_caminho.backend.dto.travel;

import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.TravelStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record TravelCreateDTO(
        @NotNull UUID pollId,
        @NotNull UUID busId,
        @NotNull TravelStatus status,
        @NotNull LocalTime returnTime,
        PollDirection direction
) {
    public TravelCreateDTO(UUID pollId, UUID busId, TravelStatus status, LocalTime returnTime) {
        this(pollId, busId, status, returnTime, PollDirection.OUTBOUND);
    }
}
