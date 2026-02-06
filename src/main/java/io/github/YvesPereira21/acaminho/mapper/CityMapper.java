package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.CityResponseDTO;
import io.github.YvesPereira21.acaminho.model.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "stateName", source = "state.stateName")
    CityResponseDTO toResponse(City city);

}