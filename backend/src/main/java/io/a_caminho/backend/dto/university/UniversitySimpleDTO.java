package io.a_caminho.backend.dto.university;

import java.util.UUID;

public record UniversitySimpleDTO(
        UUID universityId,
        String campus
) {}
