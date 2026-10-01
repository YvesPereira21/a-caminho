package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.model.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StateMapper {

    @Mapping(target = "stateId", ignore = true)
    @Mapping(target = "cities", ignore = true)
    State toEntity(StateDTO dto);

    StateDTO toResponse(State entity);
}
