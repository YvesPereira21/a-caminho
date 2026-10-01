package io.a_caminho.backend.dto.auth;

import io.a_caminho.backend.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados do usuário autenticado retornados após o login")
public record UserDTO(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Nome de exibição do usuário (estudante, motorista ou prefeitura)", example = "Carlos Alberto Silva")
        String name,

        @Schema(description = "E-mail cadastrado do usuário", example = "estudante@ufpb.br")
        String email,

        @Schema(description = "Papel/Role do usuário no sistema", example = "Estudante")
        UserRole role
) {}
