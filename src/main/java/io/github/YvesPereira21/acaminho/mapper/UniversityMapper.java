package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.UniversityDTO;
import io.github.YvesPereira21.acaminho.model.University;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UniversityMapper {

    @Mapping(source = "city.cityName", target = "cityName")
    UniversityDTO convertToEntity(University university);

    @Mapping(target = "city", source = "cityName")
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "fleet", ignore = true)
    University convertToDTO(UniversityDTO universityDTO);
}