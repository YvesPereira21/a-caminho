package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.request.StateRequestDTO;
import io.github.YvesPereira21.acaminho.model.State;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateMapper {

    StateRequestDTO toResponse(State state);

}