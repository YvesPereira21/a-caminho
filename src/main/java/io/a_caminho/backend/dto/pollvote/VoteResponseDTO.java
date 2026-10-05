package io.a_caminho.backend.dto.pollvote;

import java.time.LocalDateTime;
import java.util.UUID;

public record VoteResponseDTO(
        UUID voteId,
        UUID pollId,
        UUID optionId,
        String stopName,
        Boolean returnConfirmed,
        LocalDateTime voteTime
) {}
