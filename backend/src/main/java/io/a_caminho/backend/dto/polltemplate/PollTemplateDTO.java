package io.a_caminho.backend.dto.polltemplate;

import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.model.enums.Shift;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Dados do modelo de enquete cadastrado")
public record PollTemplateDTO(
        @Schema(description = "Identificador único do modelo", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID templateId,

        @Schema(description = "Nome da rota", example = "Rota Universitária João Pessoa")
        String routeName,

        @Schema(description = "Turno da rota", example = "MANHA")
        Shift shift,

        @Schema(description = "Horário de início da votação", example = "06:00:00")
        LocalTime defaultStartTime,

        @Schema(description = "Horário de término da votação", example = "22:00:00")
        LocalTime defaultEndTime,

        @Schema(description = "Status de ativação do modelo", example = "true")
        Boolean active,

        @Schema(description = "Universidades atendidas por este modelo")
        Set<UniversityResponseDTO> targetUniversities,

        @Schema(description = "Pontos de ônibus padrão do modelo")
        Set<PollOptionResponseDTO> defaultOptions
) {}
