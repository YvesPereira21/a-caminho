package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.BusDriverDTO;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BusDriverMapper {

    @Mapping(source = "bus.busId", target = "busId")
    @Mapping(source = "municipality.municipalityId", target = "municipalityId")
    BusDriverDTO convertToEntity(BusDriver busDriver);

    @Mapping(target = "bus", source = "busId")
    @Mapping(target = "municipality", source = "municipalityId")
    BusDriver convertToDTO(BusDriverDTO busDriverDTO);
}