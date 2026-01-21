package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.CityDTO;
import io.github.YvesPereira21.acaminho.mapper.CityMapper;
import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.service.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(name = "/cities")
public class CityController {

    private final CityService cityService;
    private final CityMapper cityMapper;

    public CityController(CityService cityService, CityMapper cityMapper) {
        this.cityService = cityService;
        this.cityMapper = cityMapper;
    }

    @PostMapping("")
    public ResponseEntity<CityDTO> createCity(@Valid @RequestBody CityDTO cityDTO) {
        City city = cityMapper.convertToEntity(cityDTO);
        City newCity = cityService.createCity(city);
        return ResponseEntity.status(HttpStatus.CREATED).body(cityMapper.convertToDTO(newCity));
    }

    @GetMapping("")
    public ResponseEntity<CityDTO> getCityByCityNameAndStateName(@RequestParam(defaultValue = "João Pessoa") String cityName,
                                                                 @RequestParam(defaultValue = "Paraíba") String stateName) {
        City city = cityService.getCityByCityNameAndStateName(cityName, stateName);
        return ResponseEntity.ok(cityMapper.convertToDTO(city));
    }

    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void> deleteCity(@PathVariable UUID cityId) {
        cityService.deleteCity(cityId);
        return ResponseEntity.noContent().build();
    }
}