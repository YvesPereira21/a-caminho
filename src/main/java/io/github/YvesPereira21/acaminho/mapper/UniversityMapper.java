package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.UniversityResponseDTO;
import io.github.YvesPereira21.acaminho.model.University;
import org.mapstruct.Mapper;

@Mapper
public interface UniversityMapper {

    UniversityResponseDTO toResponse(University university);

}