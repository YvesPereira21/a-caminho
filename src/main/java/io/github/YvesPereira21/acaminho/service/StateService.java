package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.StateRequestDTO;
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

    public StateRequestDTO createState(StateRequestDTO state){
        State stateExists = stateRepository.findByStateName(state.stateName())
                .orElseThrow();
        State newState = new State();
        newState.setStateName(state.stateName());
        return stateMapper.toResponse(stateRepository.save(newState));
    }

    public void deleteState(String stateName) {
        State state = stateRepository.findByStateName(stateName)
                .orElseThrow();
        stateRepository.delete(state);
    }
}