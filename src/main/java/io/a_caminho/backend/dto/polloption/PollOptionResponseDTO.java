package io.a_caminho.backend.dto.polloption;

import java.util.UUID;

public record PollOptionResponseDTO(
        UUID optionId,
        String stopName,
        Long voteCount
) {}
