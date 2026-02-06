package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.UniversityStudentResponseDTO;
import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UniversityStudentMapper {

    @Mapping(target = "email", source = "user.email")
    UniversityStudentResponseDTO toResponse(UniversityStudent universityStudent);

}