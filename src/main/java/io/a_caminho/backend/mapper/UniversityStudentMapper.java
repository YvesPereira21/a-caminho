package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.model.UniversityStudent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UniversityStudentMapper {

    @Mapping(target = "studentId", source = "universityStudentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "municipalityId", source = "municipality.municipalityId")
    @Mapping(target = "universityId", source = "university.universityId")
    StudentDTO toDTO(UniversityStudent student);

    @Mapping(target = "universityStudentId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "votes", ignore = true)
    UniversityStudent toEntity(StudentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "universityStudentId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "admissionDate", ignore = true)
    @Mapping(target = "votes", ignore = true)
    UniversityStudent updateEntityFromDTO(StudentUpdateDTO dto, @MappingTarget UniversityStudent entity);
}
