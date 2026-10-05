package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.PollOptionMapper;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.PollOption;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.PollOptionRepository;
import io.a_caminho.backend.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PollOptionService {

    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PollOptionMapper pollOptionMapper;
    private final MunicipalityRepository municipalityRepository;

    public PollOptionService(
            PollOptionRepository pollOptionRepository,
            PollVoteRepository pollVoteRepository,
            PollOptionMapper pollOptionMapper,
            MunicipalityRepository municipalityRepository
    ){
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.pollOptionMapper = pollOptionMapper;
        this.municipalityRepository = municipalityRepository;
    }

    public PollOptionResponseDTO createPollOption(UUID municipalityUserId, PollOptionDTO pollOption){
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));

        boolean stopExists = pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                municipality.getMunicipalityId(),
                pollOption.stopName()
        );
        if (stopExists) {
            throw new ObjectAlreadyExistsException("Já existe um ponto de ônibus cadastrado com este nome.");
        }

        PollOption newPollOption = pollOptionMapper.toEntity(pollOption);
        newPollOption.setMunicipality(municipality);

        return pollOptionMapper.toDTO(pollOptionRepository.save(newPollOption));
    }

    public PollOptionResponseDTO getPollOption(UUID municipalityUserId, UUID pollOptionId){
        PollOption pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ObjectNotFoundException("Ponto não cadastrado."));

        if(!pollOption.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        return pollOptionMapper.toDTO(pollOption);
    }

    public List<PollOptionResponseDTO> getAllPollOptions(UUID municipalityUserId){
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));

        return pollOptionRepository
                .findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(pollOptionMapper::toDTO)
                .toList();
    }

    public void updatePollOption (UUID municipalityUserId, UUID pollOptionId, PollOptionDTO pollOptionDTO){
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));
        PollOption pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ObjectNotFoundException("Ponto não cadastrado."));

        if(!pollOption.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        if (!pollOption.getStopName().equalsIgnoreCase(pollOptionDTO.stopName())) {
            boolean stopExists = pollOptionRepository.existsByMunicipality_MunicipalityIdAndStopNameIgnoreCase(
                    municipality.getMunicipalityId(),
                    pollOptionDTO.stopName()
            );
            if (stopExists) {
                throw new ObjectAlreadyExistsException("Já existe um ponto de ônibus cadastrado com este nome.");
            }
        }

        pollOptionRepository.save(pollOptionMapper.updateEntityFromDTO(pollOptionDTO, pollOption));
    }

    public void deleteOption(UUID municipalityUserId, UUID pollOptionId){
        Municipality municipality = municipalityRepository.findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));
        PollOption pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ObjectNotFoundException("Ponto não cadastrado."));

        if(!pollOption.getMunicipality().getUser().getId().equals(municipalityUserId)){
            throw new UserIsNotOwnerException("Você não tem permissão para isso.");
        }

        if (pollVoteRepository.existsByOption_OptionId(pollOptionId)) {
            throw new IllegalArgumentException("Não é possível excluir este ponto pois já existem votos vinculados a ele.");
        }

        pollOptionRepository.delete(pollOption);
    }
}
