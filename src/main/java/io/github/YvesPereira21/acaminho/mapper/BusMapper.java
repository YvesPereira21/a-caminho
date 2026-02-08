package io.github.YvesPereira21.acaminho.mapper;

import io.github.YvesPereira21.acaminho.dto.response.BusResponseDTO;
import io.github.YvesPereira21.acaminho.model.Bus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MunicipalityMapper.class, BusDriverMapper.class, UniversityMapper.class})
public interface BusMapper {

    BusResponseDTO toResponse(Bus bus);
}