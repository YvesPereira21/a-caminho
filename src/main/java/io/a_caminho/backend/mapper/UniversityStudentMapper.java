package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.student.StudentRegistrationRequestDTO;
import io.a_caminho.backend.dto.student.StudentResponseDTO;
import io.a_caminho.backend.model.UniversityStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UniversityStudentMapper {

    @Mapping(target = "studentId", source = "universityStudentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "municipalityId", source = "municipality.municipalityId")
    @Mapping(target = "universityId", source = "university.universityId")
    StudentResponseDTO toDTO(UniversityStudent student);

    @Mapping(target = "universityStudentId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "university", ignore = true)
    @Mapping(target = "votes", ignore = true)
    UniversityStudent toEntity(StudentRegistrationRequestDTO dto);
}
