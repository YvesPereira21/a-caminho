package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.CityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.CityResponseDTO;
import io.github.YvesPereira21.acaminho.security.SecurityConfigurations;
import io.github.YvesPereira21.acaminho.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "cities")
@Validated
@Tag(name = "City", description = "Controller para gerenciamento da entidade City")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    @Operation(summary = "Cria cidade", description = "Cria objeto cidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cidade criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar cidade.")
    })
    public ResponseEntity<CityResponseDTO> createCity(@RequestBody CityRequestDTO city){
        CityResponseDTO cityResponse = cityService.createCity(city);
        return ResponseEntity.status(HttpStatus.CREATED).body(cityResponse);
    }

    @GetMapping("")
    @Operation(summary = "Retorna objeto cidade", description = "Retorna objeto cidade baseado no seu nome e nome do estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cidade retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cidade não encontrada.")
    })
    public ResponseEntity<CityResponseDTO> getCityByCityNameAndStateName(@RequestParam String cityName, @RequestParam String stateName){
        return ResponseEntity.ok(cityService.getCityByCityNameAndStateName(cityName, stateName));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("")
    @Operation(summary = "Deleta objeto cidade", description = "Deleta objeto cidade baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cidade deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cidade não encontrada.")
    })
    public ResponseEntity<Void> deleteCity(@RequestParam String cityName, @RequestParam String stateName){
        cityService.deleteCity(cityName, stateName);
        return ResponseEntity.noContent().build();
    }
}
