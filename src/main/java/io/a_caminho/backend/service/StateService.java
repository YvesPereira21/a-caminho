package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.mapper.StateMapper;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.repository.StateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StateService {

    private final StateRepository stateRepository;
    private final StateMapper stateMapper;

    public StateService(StateRepository stateRepository, StateMapper stateMapper) {
        this.stateRepository = stateRepository;
        this.stateMapper = stateMapper;
    }

    @Transactional
    public StateDTO createState(StateDTO state){
        boolean stateAlreadyExists = stateRepository.existsByStateName(state.stateName());

        if(stateAlreadyExists){
            throw new ObjectAlreadyExistsException("O estado com esse nome já existe.");
        }

        State newState = stateMapper.toEntity(state);

        return stateMapper.toResponse(stateRepository.save(newState));
    }

    @Transactional(readOnly = true)
    public List<StateDTO> getAllState(){
        return stateRepository.findAll()
                .stream()
                .map(stateMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteState(String stateName) {
        State state = stateRepository.findByStateName(stateName)
                .orElseThrow(() -> new ObjectNotFoundException("O estado não existe."));

        stateRepository.delete(state);
    }
}
