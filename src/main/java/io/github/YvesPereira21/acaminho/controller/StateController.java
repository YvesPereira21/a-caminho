package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.StateDTO;
import io.github.YvesPereira21.acaminho.security.SecurityConfigurations;
import io.github.YvesPereira21.acaminho.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/states")
@Validated
@Tag(name = "State", description = "Controller para gerenciamento da entidade State")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    @Operation(summary = "Cria estado", description = "Cria objeto estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estado criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar estado.")
    })
    public ResponseEntity<StateDTO> createState(@RequestBody @Valid StateDTO stateDTO) {
        StateDTO newState = stateService.createState(stateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newState);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{stateName}")
    @Operation(summary = "Deleta objeto estado", description = "Deleta objeto estado baseado no seu nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Estado não encontrado.")
    })
    public ResponseEntity<Void> deleteState(@PathVariable String stateName) {
        stateService.deleteState(stateName);
        return ResponseEntity.noContent().build();
    }
}
