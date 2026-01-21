package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.StateDTO;
import io.github.YvesPereira21.acaminho.mapper.StateMapper;
import io.github.YvesPereira21.acaminho.model.State;
import io.github.YvesPereira21.acaminho.service.StateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(name = "/states")
public class StateController {

    private final StateService stateService;
    private final StateMapper stateMapper;

    public StateController(StateService stateService, StateMapper stateMapper) {
        this.stateService = stateService;
        this.stateMapper = stateMapper;
    }

    @PostMapping("")
    public ResponseEntity<StateDTO> createState(@Valid @RequestBody StateDTO stateDTO) {
        State state = stateMapper.convertToEntity(stateDTO);
        State newState = stateService.createState(state);
        return ResponseEntity.status(HttpStatus.CREATED).body(stateMapper.convertToDTO(newState));
    }

    @DeleteMapping("/{stateName}")
    public ResponseEntity<Void> deleteState(@PathVariable String stateName) {
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}