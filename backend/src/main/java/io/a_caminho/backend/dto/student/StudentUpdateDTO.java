package io.a_caminho.backend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Requisição para atualização da conta do estudante universitário")
public record StudentUpdateDTO(
        @Schema(description = "Nome completo do estudante", example = "Carlos Alberto Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String studentName,

        @Schema(description = "CPF do estudante (apenas dígitos ou formatado)", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
        String cpf,

        @Schema(description = "Número de matrícula institucional", example = "2023001234")
        String registrationNumber,

        @Schema(description = "Nome do curso universitário", example = "Engenharia de Software")
        String courseName,

        @Schema(description = "Período acadêmico atual", example = "4")
        Integer currentPeriod,

        @Schema(description = "Identificador único da universidade", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID universityId
) {}
