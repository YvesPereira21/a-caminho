package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.service.BusDriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/busDrivers")
public class BusDriverController {

    private final BusDriverService busDriverService;

    public BusDriverController(BusDriverService busDriverService) {
        this.busDriverService = busDriverService;
    }

    @PostMapping("")
    public ResponseEntity<BusDriverResponseDTO> createBusDriverAccount(@RequestBody BusDriverRequestDTO busDriverRequestDTO){
        BusDriverResponseDTO busDriverResponseDTO = busDriverService.createBusDriverAccount(busDriverRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(busDriverResponseDTO);
    }

    @GetMapping("/{busDriverId}")
    public ResponseEntity<BusDriverResponseDTO> getBusDriverById(@PathVariable UUID busDriverId){
        return ResponseEntity.ok(busDriverService.getBusDriverById(busDriverId));
    }

    @DeleteMapping("/{busDriverId}")
    public ResponseEntity<Void> deleteBusDriverById(@PathVariable UUID busDriverId){
        busDriverService.deleteBusDriverAccount(busDriverId);
        return ResponseEntity.noContent().build();
    }
}
