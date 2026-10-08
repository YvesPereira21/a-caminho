package io.a_caminho.backend.dto.auth;

import java.util.UUID;

public record AuthResponseDTO(
        UUID userId,
        String email,
        String role,
        String message
        ) {

}
