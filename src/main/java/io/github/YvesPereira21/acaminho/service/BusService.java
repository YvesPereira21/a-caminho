package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.Bus;
import io.github.YvesPereira21.acaminho.repository.BusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BusService {

    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public Bus createBus(Bus bus) {
        return busRepository.save(bus);
    }

    public Bus getBus(UUID busId) {
        return busRepository.findByBusId(busId).orElseThrow();
    }

    public Bus updateBus(UUID busId, Bus newBus) {
        Bus bus = getBus(busId);
        return busRepository.save(newBus);
    }

    public void deleteBus(UUID busId) {
        busRepository.deleteById(busId);
    }

    public List<Bus> findAllByMunicipalityName(String municipalityName) {
        return busRepository.findAllByMunicipality_MunicipalityName(municipalityName);
    }
}