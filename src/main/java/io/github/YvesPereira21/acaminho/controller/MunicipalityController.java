package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.MunicipalityDTO;
import io.github.YvesPereira21.acaminho.mapper.MunicipalityMapper;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.service.MunicipalityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(name = "/municipalities")
public class MunicipalityController {

    private final MunicipalityService municipalityService;
    private final MunicipalityMapper municipalityMapper;

    public MunicipalityController(MunicipalityService municipalityService, MunicipalityMapper municipalityMapper) {
        this.municipalityService = municipalityService;
        this.municipalityMapper = municipalityMapper;
    }

    @PostMapping("")
    public ResponseEntity<MunicipalityDTO> createMunicipality(@RequestBody MunicipalityDTO municipalityDTO) {
        Municipality municipality = municipalityMapper.convertToEntity(municipalityDTO);
        Municipality newMunicipality = municipalityService.createMunicipality(municipality);
        return ResponseEntity.status(HttpStatus.CREATED).body(municipalityMapper.convertToDTO(newMunicipality));
    }

    @GetMapping("/{municipalityName}")
    public ResponseEntity<MunicipalityDTO> getMunicipalityByMunicipalityName(@PathVariable String municipalityName) {
        Municipality municipality = municipalityService.getMunicipalityByMunicipalityName(municipalityName);
        return ResponseEntity.ok(municipalityMapper.convertToDTO(municipality));
    }

    @PutMapping("/{municipalityName}")
    public ResponseEntity<MunicipalityDTO> updateMunicipality(@PathVariable String municipalityName,
                                                              @RequestBody MunicipalityDTO municipalityDTO) {
        Municipality municipality = municipalityMapper.convertToEntity(municipalityDTO);
        Municipality updatedMunicipality = municipalityService.updateMunicipality(municipalityName, municipality);
        return ResponseEntity.ok(municipalityMapper.convertToDTO(updatedMunicipality));
    }

    @DeleteMapping("/{municipalityName}")
    public ResponseEntity<Void> deleteMunicipality(@PathVariable String municipalityName) {
        municipalityService.deleteMunicipality(municipalityName);
        return ResponseEntity.noContent().build();
    }
}