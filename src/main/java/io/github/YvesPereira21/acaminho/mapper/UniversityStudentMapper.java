package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.UniversityStudentDTO;
import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UniversityStudentMapper {

    @Mapping(source = "municipality.municipalityId", target = "municipalityId")
    @Mapping(source = "university.universityId", target = "universityId")
    UniversityStudentDTO convertToEntity(UniversityStudent universityStudent);

    @Mapping(target = "municipality", source = "municipalityId")
    @Mapping(target = "university", source = "universityId")
    UniversityStudent convertToDto(UniversityStudentDTO universityStudentDTO);
}