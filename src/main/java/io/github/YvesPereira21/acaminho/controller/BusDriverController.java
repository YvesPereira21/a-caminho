package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.BusDriverDTO;
import io.github.YvesPereira21.acaminho.mapper.BusDriverMapper;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.service.BusDriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(name = "busDrivers")
public class BusDriverController {

    private final BusDriverService busDriverService;
    private final BusDriverMapper busDriverMapper;

    public BusDriverController(BusDriverService busDriverService, BusDriverMapper busDriverMapper) {
        this.busDriverService = busDriverService;
        this.busDriverMapper = busDriverMapper;
    }

    @PostMapping("")
    public ResponseEntity<BusDriverDTO> createBusDriver(@RequestBody BusDriverDTO busDriverDTO) {
        BusDriver busDriver = busDriverMapper.convertToEntity(busDriverDTO);
        BusDriver newBusDriver = busDriverService.createBusDriverAccount(busDriver);
        return ResponseEntity.status(HttpStatus.CREATED).body(busDriverMapper.convertToDTO(newBusDriver));
    }

    @DeleteMapping("/{busDriverId}")
    public ResponseEntity<Void> deleteBusDriver(@PathVariable UUID busDriverId) {
        busDriverService.deleteBusDriverAccount(busDriverId);
        return ResponseEntity.noContent().build();
    }
}