package io.a_caminho.backend.dto.travel;

import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.university.UniversitySimpleDTO;
import io.a_caminho.backend.model.enums.PollDirection;

import java.util.Set;
import java.util.UUID;

public record TravelDTO(
        UUID travelId,
        PollDirection direction,
        BusDriverResponseDTO busDriver,
        Integer confirmedVotesCount,
        Set<UniversitySimpleDTO> targetUniversities
) {}
