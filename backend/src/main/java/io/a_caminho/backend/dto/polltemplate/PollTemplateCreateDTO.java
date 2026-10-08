package io.a_caminho.backend.dto.polltemplate;

import io.a_caminho.backend.model.enums.Shift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Requisição para criação de modelo de enquete")
public record PollTemplateCreateDTO(
        @NotBlank(message = "O nome da rota é obrigatório")
        @Schema(description = "Nome da rota", example = "Rota Universitária João Pessoa")
        String routeName,

        @NotNull(message = "O turno é obrigatório")
        @Schema(description = "Turno da rota", example = "MANHA")
        Shift shift,

        @NotNull(message = "O horário padrão de início é obrigatório")
        @Schema(description = "Horário de início da votação", example = "06:00:00")
        LocalTime defaultStartTime,

        @NotNull(message = "O horário padrão de término é obrigatório")
        @Schema(description = "Horário de término da votação", example = "22:00:00")
        LocalTime defaultEndTime,

        @NotEmpty(message = "Pelo menos uma universidade deve ser vinculada")
        @Size(min = 1, max = 5, message = "Deve conter entre 1 e 5 universidades vinculadas")
        @Schema(description = "IDs das universidades atendidas pela rota")
        Set<UUID> targetUniversityIds,

        @NotEmpty(message = "Pelo menos um ponto padrão deve ser vinculado")
        @Size(min = 1, message = "Deve conter pelo menos um ponto padrão")
        @Schema(description = "IDs dos pontos de ônibus padrão")
        Set<UUID> defaultOptionIds
) {}
