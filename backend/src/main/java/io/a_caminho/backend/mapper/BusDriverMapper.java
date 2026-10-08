package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.model.BusDriver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusDriverMapper {

    @Mapping(target = "email", source = "user.email")
    BusDriverResponseDTO toResponse(BusDriver busDriver);

    @Mapping(target = "busDriverId", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "travels", ignore = true)
    BusDriver toEntity(BusDriverRequestDTO busDriver);
}
