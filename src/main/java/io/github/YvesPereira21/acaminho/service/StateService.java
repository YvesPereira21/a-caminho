package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.State;
import io.github.YvesPereira21.acaminho.repository.StateRepository;
import org.springframework.stereotype.Service;

@Service
public class StateService {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public State createState(State state){
        return stateRepository.save(state);
    }

    public void deleteState(String stateName) {
        State state = stateRepository.findByStateName(stateName)
                .orElseThrow();
        stateRepository.delete(state);
    }
}