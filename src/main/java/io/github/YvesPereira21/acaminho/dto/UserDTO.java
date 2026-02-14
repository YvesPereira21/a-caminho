package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDTO(
        @NotBlank @Email String email,
        @NotBlank String password
) {}