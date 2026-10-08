package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.bus.BusCreateDTO;
import io.a_caminho.backend.dto.bus.BusDTO;
import io.a_caminho.backend.dto.bus.BusUpdateDTO;
import io.a_caminho.backend.model.Bus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BusMapper {

    BusDTO toDTO(Bus bus);

    @Mapping(target = "busId", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "travels", ignore = true)
    Bus toEntity(BusCreateDTO busCreateDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "busId", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "travels", ignore = true)
    Bus updateEntityFromDTO(BusUpdateDTO busUpdateDTO, @MappingTarget Bus bus);
}
