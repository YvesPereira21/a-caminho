package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.model.Municipality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MunicipalityMapper {

    MunicipalityResponseDTO toResponse(Municipality municipality);

    @Mapping(target = "municipalityId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "fleet", ignore = true)
    @Mapping(target = "busDrivers", ignore = true)
    @Mapping(target = "universityStudents", ignore = true)
    @Mapping(target = "travels", ignore = true)
    @Mapping(target = "polls", ignore = true)
    @Mapping(target = "pollTemplates", ignore = true)
    @Mapping(target = "pollOptions", ignore = true)
    Municipality toEntity(MunicipalityRequestDTO municipality);
}
