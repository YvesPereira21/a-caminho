package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.MunicipalityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.MunicipalityResponseDTO;
import io.github.YvesPereira21.acaminho.service.MunicipalityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/municipality")
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    public MunicipalityController(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @PostMapping("")
    public ResponseEntity<MunicipalityResponseDTO> createMunicipality(@RequestBody MunicipalityRequestDTO municipalityRequestDTO){
        MunicipalityResponseDTO municipality = municipalityService.createMunicipality(municipalityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(municipality);
    }

    @GetMapping("/{municipalityName}")
    public ResponseEntity<MunicipalityResponseDTO> getMunicipalityByName(@PathVariable String municipalityName){
        return ResponseEntity.ok(municipalityService.getMunicipalityByName(municipalityName));
    }

    @DeleteMapping("/{municipalityName}")
    public ResponseEntity<Void> deleteMunicipality(@PathVariable String municipalityName){
        municipalityService.deleteMunicipality(municipalityName);
        return ResponseEntity.noContent().build();
    }
}
