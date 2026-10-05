package io.a_caminho.backend.mapper;

import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
import io.a_caminho.backend.model.PollTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UniversityMapper.class, PollOptionMapper.class})
public interface PollTemplateMapper {

    PollTemplateDTO toDTO(PollTemplate pollTemplate);
}
