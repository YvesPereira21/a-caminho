package io.a_caminho.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de credenciais de acesso do usuário")
public record UserDTO(
        @Schema(description = "E-mail de acesso do usuário", example = "usuario@municipio.gov.br", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Senha de acesso do usuário (mínimo de 6 caracteres)", example = "senhaSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password
) {}
