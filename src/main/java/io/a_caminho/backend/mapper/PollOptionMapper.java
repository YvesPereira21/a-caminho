package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.model.PollOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PollOptionMapper {

    @Mapping(target = "optionId", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    PollOption toEntity(PollOptionDTO pollOptionDTO);

    @Mapping(target = "voteCount", ignore = true)
    PollOptionResponseDTO toDTO(PollOption pollOption);

    @Mapping(target = "optionId", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    PollOption updateEntityFromDTO(PollOptionDTO pollOptionDTO, @MappingTarget PollOption pollOption);
}
