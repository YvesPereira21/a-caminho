package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
import io.a_caminho.backend.exception.InvalidTimeException;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollTemplateMapper;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.model.PollTemplate;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollTemplateRepository;
import io.a_caminho.backend.repository.UniversityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class PollTemplateService {

    private final PollTemplateRepository pollTemplateRepository;
    private final PollTemplateMapper pollTemplateMapper;
    private final MunicipalityRepository municipalityRepository;
    private final UniversityRepository universityRepository;
    private final PollOptionRepository pollOptionRepository;

    public PollTemplateService(
            PollTemplateRepository pollTemplateRepository,
            PollTemplateMapper pollTemplateMapper,
            MunicipalityRepository municipalityRepository,
            UniversityRepository universityRepository,
            PollOptionRepository pollOptionRepository
    ) {
        this.pollTemplateRepository = pollTemplateRepository;
        this.pollTemplateMapper = pollTemplateMapper;
        this.municipalityRepository = municipalityRepository;
        this.universityRepository = universityRepository;
        this.pollOptionRepository = pollOptionRepository;
    }

    public PollTemplateDTO createPollTemplate(UUID municipalityUserId, PollTemplateCreateDTO pollTemplate) {
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));

        boolean timeIsValid = verifyTime(pollTemplate.defaultStartTime(), pollTemplate.defaultEndTime());
        if(!timeIsValid) {
            throw new InvalidTimeException("Horário de início e término não podem ser iguais.");
        }

        boolean pollExists = pollTemplateRepository.existsByMunicipality_MunicipalityIdAndRouteNameIgnoreCaseAndShift(
                municipality.getMunicipalityId(), pollTemplate.routeName(), pollTemplate.shift()
        );
        if(pollExists) {
            throw new ObjectAlreadyExistsException("Esse modelo de enquete já existe.");
        }

        List<University> universities = universityRepository.findAllById(pollTemplate.targetUniversityIds());
        if (universities.size() != pollTemplate.targetUniversityIds().size()) {
            throw new ObjectNotFoundException("Uma ou mais universidades informadas não foram encontradas.");
        }

        List<PollOption> options = pollOptionRepository.findAllById(pollTemplate.defaultOptionIds());
        if (options.size() != pollTemplate.defaultOptionIds().size()) {
            throw new ObjectNotFoundException("Um ou mais pontos informados não foram encontrados.");
        }

        boolean allBelongToMunicipality = options.stream()
                .allMatch(o -> o.getMunicipality().getMunicipalityId().equals(municipality.getMunicipalityId()));
        if (!allBelongToMunicipality) {
            throw new UserIsNotOwnerException("Um ou mais pontos não pertencem à sua prefeitura.");
        }

        PollTemplate newPollTemplate = PollTemplate.builder()
                .routeName(pollTemplate.routeName())
                .shift(pollTemplate.shift())
                .defaultStartTime(pollTemplate.defaultStartTime())
                .defaultEndTime(pollTemplate.defaultEndTime())
                .active(true)
                .municipality(municipality)
                .targetUniversities(new HashSet<>(universities))
                .defaultOptions(new HashSet<>(options))
                .build();

        return pollTemplateMapper.toDTO(pollTemplateRepository.save(newPollTemplate));
    }

    public PollTemplateDTO getPollTemplate(UUID municipalityUserId, UUID pollTemplateId){
        PollTemplate pollTemplate = pollTemplateRepository.findById(pollTemplateId)
                .orElseThrow(() -> new ObjectNotFoundException("Modelo de enquete não cadastrado."));

        if(!pollTemplate.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        return pollTemplateMapper.toDTO(pollTemplate);
    }

    public List<PollTemplateDTO> getAllPollTemplateFromMunicipality(UUID municipalityUserId){
        return pollTemplateRepository
                .findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(pollTemplateMapper::toDTO)
                .toList();
    }

    public void deactivatePollTemplate(UUID municipalityUserId, UUID pollTemplateId){
        PollTemplate pollTemplate = pollTemplateRepository.findById(pollTemplateId)
                .orElseThrow(() -> new ObjectNotFoundException("Modelo de enquete não cadastrado."));

        if(!pollTemplate.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        pollTemplate.setActive(false);
        pollTemplateRepository.save(pollTemplate);
    }

    public void deletePollTemplate(UUID municipalityUserId, UUID pollTemplateId){
        PollTemplate pollTemplate = pollTemplateRepository.findById(pollTemplateId)
                .orElseThrow(() -> new ObjectNotFoundException("Modelo de enquete não cadastrado."));

        if(!pollTemplate.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        pollTemplateRepository.delete(pollTemplate);
    }

    private boolean verifyTime(LocalTime startTime, LocalTime endTime) {
        return !startTime.equals(endTime);
    }
}
