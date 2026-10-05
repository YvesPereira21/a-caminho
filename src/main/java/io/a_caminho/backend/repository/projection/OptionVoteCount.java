package io.a_caminho.backend.repository.projection;

import java.util.UUID;

public interface OptionVoteCount {
    UUID getOptionId();
    Long getTotal();
}
