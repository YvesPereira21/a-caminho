package io.github.YvesPereira21.acaminho.dto.response;

public record UniversityStudentResponseDTO(
        String studentName,
        String email,
        MunicipalityResponseDTO municipality,
        UniversityResponseDTO university
) {}
