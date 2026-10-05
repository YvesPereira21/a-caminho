package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.model.Poll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UniversityMapper.class, PollOptionMapper.class})
public interface PollMapper {

    PollDTO toDTO(Poll poll);

    PollListDTO toDTOList(Poll poll);
}
