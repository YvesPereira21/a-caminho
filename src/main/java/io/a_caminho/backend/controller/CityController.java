package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.CityControllerOpenApi;
import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController implements CityControllerOpenApi {

    private final CityService cityService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CityResponseDTO> createCity(@Valid @RequestBody CityRequestDTO cityRequestDTO) {
        log.info("Request to create city: {} in state: {}", cityRequestDTO.cityName(), cityRequestDTO.stateName());
        CityResponseDTO createdCity = cityService.createCity(cityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @Override
    @GetMapping("/{cityId}")
    public ResponseEntity<CityResponseDTO> getCityById(@PathVariable UUID cityId) {
        log.info("Request to fetch city by ID: {}", cityId);
        CityResponseDTO city = cityService.getCityById(cityId);
        return ResponseEntity.ok(city);
    }

    @Override
    @GetMapping("/state/{stateName}")
    public ResponseEntity<List<CityResponseDTO>> getAllCityFromStateByStateName(@PathVariable String stateName) {
        log.info("Request to fetch all cities for state: {}", stateName);
        List<CityResponseDTO> cities = cityService.getAllCityFromStateByStateName(stateName);
        return ResponseEntity.ok(cities);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void> deleteCity(@PathVariable UUID cityId) {
        log.info("Request to delete city by ID: {}", cityId);
        cityService.deleteCity(cityId);
        return ResponseEntity.noContent().build();
    }
}
