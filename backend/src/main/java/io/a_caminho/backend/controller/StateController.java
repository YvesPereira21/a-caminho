package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.StateControllerOpenApi;
import io.a_caminho.backend.dto.state.StateDTO;
import io.a_caminho.backend.service.StateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StateController implements StateControllerOpenApi {

    private final StateService stateService;

    @Override
    @GetMapping
    public ResponseEntity<List<StateDTO>> getAllState() {
        log.info("Request to fetch all states");
        List<StateDTO> states = stateService.getAllState();
        return ResponseEntity.ok(states);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StateDTO> createState(@Valid @RequestBody StateDTO stateDTO) {
        log.info("Request to create state: {}", stateDTO.stateName());
        StateDTO createdState = stateService.createState(stateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdState);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{stateName}")
    public ResponseEntity<Void> deleteState(@PathVariable String stateName) {
        log.info("Request to delete state: {}", stateName);
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}
