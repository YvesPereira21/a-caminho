package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.StateRequestDTO;
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
    public ResponseEntity<StateRequestDTO> createState(@RequestBody @Valid StateRequestDTO stateRequestDTO) {
        StateRequestDTO newState = stateService.createState(stateRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newState);
    }

    @DeleteMapping("/{stateName}")
    public ResponseEntity<Void> deleteState(@PathVariable String stateName) {
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}
