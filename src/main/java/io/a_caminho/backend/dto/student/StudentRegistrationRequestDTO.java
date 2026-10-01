package io.a_caminho.backend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Requisição para cadastro de novo estudante universitário")
public record StudentRegistrationRequestDTO(
        @Schema(description = "Nome completo do estudante", example = "Carlos Alberto Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome é obrigatório")
        String studentName,

        @Schema(description = "E-mail do estudante", example = "carlos.alberto@academico.ufpb.br", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Senha de acesso (mínimo de 6 caracteres)", example = "senhaSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @Schema(description = "CPF do estudante (apenas dígitos ou formatado)", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O CPF é obrigatório")
        String cpf,

        @Schema(description = "Número de matrícula institucional", example = "2023001234")
        String registrationNumber,

        @Schema(description = "Nome do curso universitário", example = "Engenharia de Software")
        String courseName,

        @Schema(description = "Período acadêmico atual", example = "4")
        Integer currentPeriod,

        @Schema(description = "Data de ingresso no curso", example = "2023-03-01")
        LocalDate admissionDate,

        @Schema(description = "Identificador único do município de residência", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID do município é obrigatório")
        UUID municipalityId,

        @Schema(description = "Identificador único da universidade que frequenta", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID da universidade é obrigatório")
        UUID universityId
) {}
