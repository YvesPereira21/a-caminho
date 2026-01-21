package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.BusDTO;
import io.github.YvesPereira21.acaminho.model.Bus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BusMapper {

    @Mapping(source = "municipality.municipalityId", target = "municipalityId")
    BusDTO convertToDTO(Bus bus);

    @Mapping(target = "municipality", source = "municipalityId")
    @Mapping(target = "busDriver", ignore = true)
    Bus convertToEntity(BusDTO busDTO);
}