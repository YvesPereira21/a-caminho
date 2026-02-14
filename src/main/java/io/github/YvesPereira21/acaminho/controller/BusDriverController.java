package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.security.SecurityConfigurations;
import io.github.YvesPereira21.acaminho.service.BusDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/busDrivers")
@Validated
@Tag(name = "BusDriver", description = "Controller para gerenciamento da entidade BusDriver")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class BusDriverController {

    private final BusDriverService busDriverService;

    public BusDriverController(BusDriverService busDriverService) {
        this.busDriverService = busDriverService;
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PostMapping("")
    @Operation(summary = "Cria motorista", description = "Cria uma conta para o motorista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta do motorista criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar motorista.")
    })
    public ResponseEntity<BusDriverResponseDTO> createBusDriverAccount(@RequestBody @Valid BusDriverRequestDTO busDriverRequestDTO, @AuthenticationPrincipal User user){
        BusDriverResponseDTO busDriverResponseDTO = busDriverService.createBusDriverAccount(busDriverRequestDTO, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(busDriverResponseDTO);
    }

    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'BUSDRIVER')")
    @GetMapping("/{busDriverId}")
    @Operation(summary = "Retorna objeto motorista", description = "Retorna objeto motorista baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Motorista retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Motorista não encontrado.")
    })
    public ResponseEntity<BusDriverResponseDTO> getBusDriverById(@PathVariable UUID busDriverId, @AuthenticationPrincipal User user){
        return ResponseEntity.ok(busDriverService.getBusDriverById(busDriverId, user.getUserId()));
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @DeleteMapping("/{busDriverId}")
    @Operation(summary = "Deleta conta do motorista", description = "Deleta conta do motorista baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta do motorista deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Motorista não encontrado.")
    })
    public ResponseEntity<Void> deleteBusDriverById(@PathVariable UUID busDriverId, @AuthenticationPrincipal User user){
        busDriverService.deleteBusDriverAccount(busDriverId, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
