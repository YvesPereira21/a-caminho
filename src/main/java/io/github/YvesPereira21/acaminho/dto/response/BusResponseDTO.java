package io.github.YvesPereira21.acaminho.dto.response;

import java.util.List;
import java.util.UUID;

public record BusResponseDTO(
        UUID busId,
        String busName,
        MunicipalityResponseDTO municipality,
        BusDriverResponseDTO busDriver,
        List<UniversityStudentResponseDTO> universities
) {}
