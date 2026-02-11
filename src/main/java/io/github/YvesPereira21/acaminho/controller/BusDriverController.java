package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.service.BusDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/busDrivers")
@Validated
@Tag(name = "BusDriver", description = "Controller para gerenciamento da entidade BusDriver")
//@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class BusDriverController {

    private final BusDriverService busDriverService;

    public BusDriverController(BusDriverService busDriverService) {
        this.busDriverService = busDriverService;
    }

    @PostMapping("")
    @Operation(summary = "Cria motorista", description = "Cria uma conta para o motorista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta do motorista criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar motorista.")
    })
    public ResponseEntity<BusDriverResponseDTO> createBusDriverAccount(@RequestBody BusDriverRequestDTO busDriverRequestDTO){
        BusDriverResponseDTO busDriverResponseDTO = busDriverService.createBusDriverAccount(busDriverRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(busDriverResponseDTO);
    }

    @GetMapping("/{busDriverId}")
    @Operation(summary = "Retorna objeto motorista", description = "Retorna objeto motorista baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Motorista retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Motorista não encontrado.")
    })
    public ResponseEntity<BusDriverResponseDTO> getBusDriverById(@PathVariable UUID busDriverId){
        return ResponseEntity.ok(busDriverService.getBusDriverById(busDriverId));
    }

    @DeleteMapping("/{busDriverId}")
    @Operation(summary = "Deleta conta do motorista", description = "Deleta conta do motorista baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta do motorista deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Motorista não encontrado.")
    })
    public ResponseEntity<Void> deleteBusDriverById(@PathVariable UUID busDriverId){
        busDriverService.deleteBusDriverAccount(busDriverId);
        return ResponseEntity.noContent().build();
    }
}
