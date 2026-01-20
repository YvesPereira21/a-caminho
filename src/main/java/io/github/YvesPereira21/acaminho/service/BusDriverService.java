package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BusDriverService {

    private final BusDriverRepository busDriverRepository;

    public BusDriverService(BusDriverRepository busDriverRepository) {
        this.busDriverRepository = busDriverRepository;
    }

    public BusDriver createBusDriverAccount(BusDriver busDriver) {
        return busDriverRepository.save(busDriver);
    }

    public void deleteBusDriverAccount(UUID busDriverId) {
        BusDriver busDriver = busDriverRepository.findByBusDriverId(busDriverId)
                        .orElseThrow();
        busDriverRepository.delete(busDriver);
    }
}