package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.StateDTO;
import io.github.YvesPereira21.acaminho.exception.ObjectAlreadyExistsException;
import io.github.YvesPereira21.acaminho.exception.ObjectNotFoundException;
import io.github.YvesPereira21.acaminho.mapper.StateMapper;
import io.github.YvesPereira21.acaminho.model.State;
import io.github.YvesPereira21.acaminho.repository.StateRepository;
import org.springframework.stereotype.Service;

@Service
public class StateService {

    private final StateRepository stateRepository;
    private final StateMapper stateMapper;

    public StateService(StateRepository stateRepository, StateMapper stateMapper) {
        this.stateRepository = stateRepository;
        this.stateMapper = stateMapper;
    }

    public StateDTO createState(StateDTO state){
        boolean stateAlreadyExists = stateRepository.existsByStateName(state.stateName());
        if(stateAlreadyExists){
            throw new ObjectAlreadyExistsException("O estado com esse nome já existe.");
        }
        State newState = new State();
        newState.setStateName(state.stateName());
        return stateMapper.toResponse(stateRepository.save(newState));
    }

    public void deleteState(String stateName) {
        State state = stateRepository.findByStateName(stateName)
                .orElseThrow(() -> new ObjectNotFoundException("O estado não existe."));
        stateRepository.delete(state);
    }
}