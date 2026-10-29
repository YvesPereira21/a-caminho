package io.a_caminho.backend.repository.projection;

import java.util.UUID;

public interface UniversityVoteCount {
    UUID getUniversityId();
    Long getTotal();
}
