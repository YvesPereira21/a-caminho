package io.a_caminho.backend.dto.poll;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados do estudante passageiro confirmado na enquete")
public record PollPassengerDTO(
        @Schema(description = "Identificador único do voto", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID voteId,

        @Schema(description = "Identificador único do estudante universitário", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID studentId,

        @Schema(description = "Nome do estudante", example = "Maria Silva")
        String studentName,

        @Schema(description = "Matrícula acadêmica do estudante", example = "2023001234")
        String registrationNumber,

        @Schema(description = "Curso de graduação do estudante", example = "Ciência da Computação")
        String courseName,

        @Schema(description = "Identificador único do ponto de ônibus escolhido", example = "123e4567-e89b-12d3-a456-426614174002")
        UUID optionId,

        @Schema(description = "Nome do ponto de parada de embarque", example = "Praça Central")
        String stopName,

        @Schema(description = "Indica se o estudante confirmou o retorno no mesmo dia", example = "true")
        Boolean returnConfirmed,

        @Schema(description = "Horário em que o voto foi registrado", example = "2026-10-05T07:15:30")
        LocalDateTime voteTime
) {}
