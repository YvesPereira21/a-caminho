package io.a_caminho.backend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Dados do estudante universitário cadastrado retornados pela API")
public record StudentResponseDTO(
        @Schema(description = "Identificador único do estudante universitário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID studentId,

        @Schema(description = "Identificador único da conta de usuário vinculada", example = "123e4567-e89b-12d3-a456-426614174099")
        UUID userId,

        @Schema(description = "Nome completo do estudante", example = "Carlos Alberto Silva")
        String studentName,

        @Schema(description = "E-mail cadastrado", example = "carlos.alberto@academico.ufpb.br")
        String email,

        @Schema(description = "CPF do estudante", example = "12345678901")
        String cpf,

        @Schema(description = "Matrícula institucional", example = "2023001234")
        String registrationNumber,

        @Schema(description = "Curso universitário", example = "Engenharia de Software")
        String courseName,

        @Schema(description = "Período acadêmico atual", example = "4")
        Integer currentPeriod,

        @Schema(description = "Data de admissão/ingresso", example = "2023-03-01")
        LocalDate admissionDate,

        @Schema(description = "ID do município associado", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID municipalityId,

        @Schema(description = "ID da universidade associada", example = "123e4567-e89b-12d3-a456-426614174002")
        UUID universityId
) {}
