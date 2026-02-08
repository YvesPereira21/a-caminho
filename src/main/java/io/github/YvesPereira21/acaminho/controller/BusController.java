package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.BusRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusResponseDTO;
import io.github.YvesPereira21.acaminho.service.BusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/fleet")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PostMapping("")
    public ResponseEntity<BusResponseDTO> createBus(@RequestBody BusRequestDTO busRequestDTO) {
        BusResponseDTO busResponseDTO = busService.createBus(busRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(busResponseDTO);
    }

    @GetMapping("/{busId}")
    public ResponseEntity<BusResponseDTO> getBusById(@PathVariable UUID busId){
        return ResponseEntity.ok(busService.getBus(busId));
    }

    @GetMapping("/{municipalityName}")
    public ResponseEntity<List<BusResponseDTO>> getAllBusByMunicipalityName(@PathVariable String municipalityName) {
        return ResponseEntity.ok(busService.findAllByMunicipalityName(municipalityName));
    }

    @DeleteMapping("/{busId}")
    public ResponseEntity<Void> deleteBusById(@PathVariable UUID busId){
        busService.deleteBus(busId);
        return ResponseEntity.noContent().build();
    }
}
