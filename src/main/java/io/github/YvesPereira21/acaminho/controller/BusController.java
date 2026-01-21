package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.BusDTO;
import io.github.YvesPereira21.acaminho.mapper.BusMapper;
import io.github.YvesPereira21.acaminho.model.Bus;
import io.github.YvesPereira21.acaminho.service.BusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/fleet")
public class BusController {

    private final BusService busService;
    private final BusMapper busMapper;

    public BusController(BusService busService, BusMapper busMapper) {
        this.busService = busService;
        this.busMapper = busMapper;
    }

    @PostMapping("")
    public ResponseEntity<BusDTO> createBus(@RequestBody BusDTO busDTO) {
        Bus bus = busMapper.convertToEntity(busDTO);
        Bus newBus = busService.createBus(bus);
        return ResponseEntity.status(HttpStatus.CREATED).body(busMapper.convertToDTO(newBus));
    }

    @GetMapping("/{busId}")
    public ResponseEntity<BusDTO> getBus(@PathVariable UUID busId) {
        Bus bus = busService.getBus(busId);
        return ResponseEntity.ok(busMapper.convertToDTO(bus));
    }

    @PutMapping("/{busId}")
    public ResponseEntity<BusDTO> updateBus(@PathVariable UUID busId, @RequestBody BusDTO busDTO) {
        Bus bus = busMapper.convertToEntity(busDTO);
        Bus busUpdated = busService.updateBus(busId, bus);
        return ResponseEntity.ok(busMapper.convertToDTO(busUpdated));
    }

    @DeleteMapping("/{busId}")
    public ResponseEntity<BusDTO> deleteBus(@PathVariable UUID busId) {
        busService.deleteBus(busId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/municipalities/{municipalityName}")
    public ResponseEntity<List<BusDTO>> getBusesByMunicipality(@PathVariable String municipalityName) {
        List<BusDTO> municipalityBuses = busService.findAllByMunicipalityName(municipalityName)
                .stream()
                .map(busMapper::convertToDTO)
                .toList();
        return ResponseEntity.ok(municipalityBuses);
    }
}