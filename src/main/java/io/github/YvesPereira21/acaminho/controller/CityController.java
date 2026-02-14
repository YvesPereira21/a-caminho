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

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/{cityId}")
    @Operation(summary = "Retorna objeto cidade", description = "Retorna objeto cidade baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cidade não encontrada.")
    })
    public ResponseEntity<CityResponseDTO> getCityByCityId(@PathVariable UUID cityId){
        return ResponseEntity.ok(cityService.getCityById(cityId));
    }

    @GetMapping("/{stateName}/states")
    @Operation(summary = "Retorna lista de cidades", description = "Retorna uma lista de cidade baseado no nome do estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cidades retornadas com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dado inválido.")
    })
    public ResponseEntity<List<CityResponseDTO>> getAllCityFromState(@PathVariable String stateName){
        return ResponseEntity.ok(cityService.getAllCityFromStateByStateName(stateName));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cityId}")
    @Operation(summary = "Deleta objeto cidade", description = "Deleta objeto cidade baseado no seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cidade deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cidade não encontrada.")
    })
    public ResponseEntity<Void> deleteCity(@PathVariable UUID cityId){
        cityService.deleteCity(cityId);
        return ResponseEntity.noContent().build();
    }
}
