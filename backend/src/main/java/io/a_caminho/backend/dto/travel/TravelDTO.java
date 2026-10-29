package io.a_caminho.backend.dto.travel;

import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.dto.university.UniversitySimpleDTO;
import io.a_caminho.backend.model.enums.PollDirection;
import io.a_caminho.backend.model.enums.Shift;
import io.a_caminho.backend.model.enums.TravelStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record TravelDTO(
        UUID travelId,
        TravelStatus status,
        PollDirection direction,
        LocalDate travelDate,
        LocalTime departureTime,
        LocalTime returnTime,
        Shift shift,
        BusDTO bus,
        UUID pollId,
        BusDriverResponseDTO busDriver,
        Integer confirmedVotesCount,
        Set<UniversitySimpleDTO> targetUniversities
) {}
