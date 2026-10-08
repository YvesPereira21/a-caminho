package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.model.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "cityId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "universities", ignore = true)
    City toEntity(CityRequestDTO dto);

    @Mapping(target = "stateName", source = "state.stateName")
    CityResponseDTO toResponse(City entity);
}
