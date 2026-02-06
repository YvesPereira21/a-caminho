package io.github.YvesPereira21.acaminho.dto.request;

import io.github.YvesPereira21.acaminho.dto.UserDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BusDriverRequestDTO(
        @NotBlank String busDriverName,
        @NotNull @Valid UserDTO user
) {}