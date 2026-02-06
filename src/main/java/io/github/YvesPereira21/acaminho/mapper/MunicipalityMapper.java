package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.MunicipalityResponseDTO;
import io.github.YvesPereira21.acaminho.model.Municipality;
import org.mapstruct.Mapper;

@Mapper
public interface MunicipalityMapper {

    MunicipalityResponseDTO toResponse(Municipality municipality);

}