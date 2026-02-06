package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UniversityStudentRequestDTO(
        @NotBlank String studentName,
        @NotNull @Valid UserDTO user,
        @NotNull UUID municipalityId,
        @NotNull UUID universityId
) {}