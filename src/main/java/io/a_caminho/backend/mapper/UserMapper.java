package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.auth.UserDTO;
import io.a_caminho.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "name", expression = "java(user.getName())")
    UserDTO toDTO(User user);
}
