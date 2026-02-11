package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.MunicipalityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.MunicipalityResponseDTO;
import io.github.YvesPereira21.acaminho.service.MunicipalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/municipality")
@Validated
@Tag(name = "Municipality", description = "Controller para gerenciamento da entidade Municipality")
//@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    public MunicipalityController(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @PostMapping("")
    @Operation(summary = "Cria prefeitura", description = "Cria uma conta para a prefeitura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prefeitura criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro ao criar prefeitura.")
    })
    public ResponseEntity<MunicipalityResponseDTO> createMunicipality(@RequestBody MunicipalityRequestDTO municipalityRequestDTO){
        MunicipalityResponseDTO municipality = municipalityService.createMunicipality(municipalityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(municipality);
    }

    @GetMapping("/{municipalityName}")
    @Operation(summary = "Retorna objeto prefeitura", description = "Retorna objeto prefeitura baseado no seu nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prefeitura retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Prefeitura não encontrada.")
    })
    public ResponseEntity<MunicipalityResponseDTO> getMunicipalityByName(@PathVariable String municipalityName){
        return ResponseEntity.ok(municipalityService.getMunicipalityByName(municipalityName));
    }

    @DeleteMapping("/{municipalityName}")
    @Operation(summary = "Deleta conta da prefeitura", description = "Deleta conta da prefeitura baseado no seu nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Prefeitura deletada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Prefeitura não encontrada.")
    })
    public ResponseEntity<Void> deleteMunicipality(@PathVariable String municipalityName){
        municipalityService.deleteMunicipality(municipalityName);
        return ResponseEntity.noContent().build();
    }
}
