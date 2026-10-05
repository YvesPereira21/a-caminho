package io.a_caminho.backend.dto.poll;

import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.model.enums.Shift;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Detalhes da enquete com opções e contagem de votos")
public record PollDTO(
        @Schema(description = "Identificador único da enquete", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID pollId,

        @Schema(description = "Nome da rota", example = "Rota Universitária João Pessoa")
        String routeName,

        @Schema(description = "Turno da rota", example = "MANHA")
        Shift shift,

        @Schema(description = "Data da enquete", example = "2026-10-05")
        LocalDate pollDate,

        @Schema(description = "Horário de início da votação", example = "2026-10-05T06:00:00")
        LocalDateTime startTime,

        @Schema(description = "Horário de término da votação", example = "2026-10-05T22:00:00")
        LocalDateTime endTime,

        @Schema(description = "Capacidade total de assentos disponíveis calculada a partir das viagens", example = "45")
        Integer totalCapacity,

        @Schema(description = "Total de votos confirmados na enquete", example = "38")
        Integer confirmedVotesCount,

        @Schema(description = "Universidades atendidas por esta rota")
        Set<UniversityResponseDTO> targetUniversities,

        @Schema(description = "Opções de paradas de ônibus e suas contagens de votos")
        Set<PollOptionResponseDTO> options
) {}
