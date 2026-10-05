package io.a_caminho.backend.dto.pollvote;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VoteRequestDTO(
        @NotNull(message = "O ponto de embarque é obrigatório.")
        UUID optionId,

        Boolean returnConfirmed
) {}
