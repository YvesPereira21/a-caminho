package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.model.Travel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TravelMapper {

    @Mapping(target = "travelId", ignore = true)
    @Mapping(target = "travelDate", ignore = true)
    @Mapping(target = "targetUniversities", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "poll", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "estimatedDurationMinutes", ignore = true)
    @Mapping(target = "direction", ignore = true)
    @Mapping(target = "departureTime", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "busDriver", ignore = true)
    @Mapping(target = "bus", ignore = true)
    Travel toEntity(TravelCreateDTO travelDTO);


    @Mapping(target = "confirmedVotesCount", source = "poll.confirmedVotesCount")
    @Mapping(target = "busDriver.email", source = "busDriver.user.email")
    TravelDTO toDTO(Travel travel);
}
