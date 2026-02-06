package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BusDriverMapper {

    @Mapping(target = "email", source = "user.email")
    BusDriverResponseDTO toResponse(BusDriver busDriver);

}