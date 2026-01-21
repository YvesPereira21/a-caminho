package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.MunicipalityDTO;
import io.github.YvesPereira21.acaminho.model.Municipality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MunicipalityMapper {

    @Mapping(source = "city.cityId", target = "cityId")
    MunicipalityDTO convertToDTO(Municipality municipality);

    @Mapping(target = "city", source = "cityId")
    @Mapping(target = "fleet", ignore = true)
    @Mapping(target = "busDrivers", ignore = true)
    @Mapping(target = "universityStudents", ignore = true)
    Municipality convertToEntity(MunicipalityDTO municipalityDTO);
}