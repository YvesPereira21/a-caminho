package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.StateDTO;
import io.github.YvesPereira21.acaminho.service.StateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PostMapping("")
    public ResponseEntity<StateDTO> createState(@RequestBody @Valid StateDTO stateDTO) {
        StateDTO newState = stateService.createState(stateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newState);
    }

    @DeleteMapping("/{stateName}")
    public ResponseEntity<Void> deleteState(@PathVariable String stateName) {
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}
