package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.BusRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusResponseDTO;
import io.github.YvesPereira21.acaminho.mapper.BusMapper;
import io.github.YvesPereira21.acaminho.model.Bus;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import io.github.YvesPereira21.acaminho.repository.BusRepository;
import io.github.YvesPereira21.acaminho.repository.UniversityRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final BusDriverRepository busDriverRepository;
    private final UniversityRepository universityRepository;
    private final BusMapper busMapper;

    public BusService(BusRepository busRepository, BusDriverRepository busDriverRepository, UniversityRepository universityRepository, BusMapper busMapper) {
        this.busRepository = busRepository;
        this.busDriverRepository = busDriverRepository;
        this.universityRepository = universityRepository;
        this.busMapper = busMapper;
    }

    public BusResponseDTO createBus(BusRequestDTO bus) {
        BusDriver getBusDriver = busDriverRepository.findByBusDriverId(bus.busDriverId())
                .orElseThrow();
        List<University> universities = new ArrayList<>();
        Bus newBus = new Bus();
        newBus.setBusName(bus.busName());
        newBus.setBusDriver(getBusDriver);
        for (UUID universityId : bus.universityIds()) {
            University university = universityRepository.findById(universityId)
                    .orElseThrow();
            universities.add(university);
        }
        newBus.setUniversities(universities);
        return busMapper.toResponse(busRepository.save(newBus));
    }

    public BusResponseDTO getBus(UUID busId) {
        Bus bus = busRepository.findByBusId(busId)
                .orElseThrow();
        return busMapper.toResponse(bus);
    }

    public BusResponseDTO updateBus(UUID busId, BusRequestDTO newBus) {
        Bus bus = busRepository.findByBusId(busId)
                .orElseThrow();
        return busMapper.toResponse(busRepository.save(bus));
    }

    public void deleteBus(UUID busId) {
        Bus bus = busRepository.findByBusId(busId)
                .orElseThrow();
        busRepository.deleteById(busId);
    }

    public List<Bus> findAllByMunicipalityName(String municipalityName) {
        return busRepository.findAllByMunicipality_MunicipalityName(municipalityName);
    }
}