package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.BusRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusResponseDTO;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.security.SecurityConfigurations;
import io.github.YvesPereira21.acaminho.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/fleet")
@Validated
@Tag(name = "Bus", description = "Controller para gerenciamento da entidade Bus")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PostMapping("")
    @Operation(summary = "Cria ônibus", description = "Cria um objeto ônibus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ônibus criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar ônibus.")
    })
    public ResponseEntity<BusResponseDTO> createBus(@RequestBody BusRequestDTO busRequestDTO, @AuthenticationPrincipal User user) {
        BusResponseDTO busResponseDTO = busService.createBus(busRequestDTO, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(busResponseDTO);
    }

    @GetMapping("/{busId}")
    @Operation(summary = "Retorna objeto ônibus", description = "Retorna objeto ônibus baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ônibus retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Ônibus não encontrado.")
    })
    public ResponseEntity<BusResponseDTO> getBusById(@PathVariable UUID busId){
        return ResponseEntity.ok(busService.getBus(busId));
    }

    @GetMapping("municipality/{municipalityName}")
    @Operation(summary = "Lista de ônibus de uma prefeitura", description = "Retorna os ônibus de acordo com o nome da prefeitura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ônibus retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Não existe uma prefeitura com esse nome.")
    })
    public ResponseEntity<List<BusResponseDTO>> getAllBusByMunicipalityName(@PathVariable String municipalityName) {
        return ResponseEntity.ok(busService.findAllByMunicipalityName(municipalityName));
    }

    @PreAuthorize("hasRole('MUNICIPALITY')")
    @DeleteMapping("/{busId}")
    @Operation(summary = "Deleta objeto ônibus", description = "Deleta objeto ônibus baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ônibus deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Ônibus não encontrado.")
    })
    public ResponseEntity<Void> deleteBusById(@PathVariable UUID busId){
        busService.deleteBus(busId);
        return ResponseEntity.noContent().build();
    }
}
