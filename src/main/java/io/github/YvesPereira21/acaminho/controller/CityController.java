package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.CityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.CityResponseDTO;
import io.github.YvesPereira21.acaminho.service.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping("")
    public ResponseEntity<CityResponseDTO> createCity(@RequestBody CityRequestDTO city){
        CityResponseDTO cityResponse = cityService.createCity(city);
        return ResponseEntity.status(HttpStatus.CREATED).body(cityResponse);
    }

    @GetMapping("")
    public ResponseEntity<CityResponseDTO> getCityByCityNameAndStateName(@RequestParam String cityName, @RequestParam String stateName){
        return ResponseEntity.ok(cityService.getCityByCityNameAndStateName(cityName, stateName));
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteCity(@RequestParam String cityName, @RequestParam String stateName){
        cityService.deleteCity(cityName, stateName);
        return ResponseEntity.noContent().build();
    }
}
