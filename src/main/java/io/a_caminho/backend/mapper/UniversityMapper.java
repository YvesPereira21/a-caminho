package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.model.University;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CityMapper.class})
public interface UniversityMapper {

    @Mapping(source = "universityId", target = "id")
    @Mapping(source = "city.cityName", target = "cityName")
    @Mapping(source = "city.state.stateName", target = "stateName")
    UniversityResponseDTO toResponse(University university);

    @Mapping(target = "universityId", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "defaultBuses", ignore = true)
    @Mapping(target = "polls", ignore = true)
    @Mapping(target = "travels", ignore = true)
    University toEntity(UniversityRequestDTO university);
}
