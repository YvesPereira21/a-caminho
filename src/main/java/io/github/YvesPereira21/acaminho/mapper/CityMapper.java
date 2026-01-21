package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.CityDTO;
import io.github.YvesPereira21.acaminho.model.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(source = "state.stateName", target = "stateName")
    CityDTO convertToDTO(City city);

    @Mapping(target = "state", source = "stateName")
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "universities", ignore = true)
    City convertToEntity(CityDTO cityDTO);
}