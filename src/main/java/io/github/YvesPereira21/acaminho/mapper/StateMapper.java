package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.StateDTO;
import io.github.YvesPereira21.acaminho.model.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StateMapper {

    StateDTO convertToDTO(State state);

    @Mapping(target = "cities", ignore = true)
    State convertToEntity(StateDTO stateDTO);
}