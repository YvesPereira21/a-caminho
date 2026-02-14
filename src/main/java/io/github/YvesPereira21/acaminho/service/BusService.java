package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.BusRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusResponseDTO;
import io.github.YvesPereira21.acaminho.exception.CrossMunicipalityAccessException;
import io.github.YvesPereira21.acaminho.exception.ObjectNotFoundException;
import io.github.YvesPereira21.acaminho.mapper.BusMapper;
import io.github.YvesPereira21.acaminho.model.Bus;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import io.github.YvesPereira21.acaminho.repository.BusRepository;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import io.github.YvesPereira21.acaminho.repository.UniversityRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final BusDriverRepository busDriverRepository;
    private final UniversityRepository universityRepository;
    private final MunicipalityRepository municipalityRepository;
    private final BusMapper busMapper;

    public BusService(BusRepository busRepository, BusDriverRepository busDriverRepository, UniversityRepository universityRepository, MunicipalityRepository municipalityRepository, BusMapper busMapper) {
        this.busRepository = busRepository;
        this.busDriverRepository = busDriverRepository;
        this.universityRepository = universityRepository;
        this.municipalityRepository = municipalityRepository;
        this.busMapper = busMapper;
    }

    public BusResponseDTO createBus(BusRequestDTO bus, UUID municipalityUserId) {
        Municipality municipality = municipalityRepository
                .findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));
        BusDriver getBusDriver = busDriverRepository
                .findByBusDriverId(bus.busDriverId())
                .orElseThrow(() -> new ObjectNotFoundException("Motorista não encontrado."));

        List<University> universities = new ArrayList<>();
        Bus newBus = new Bus();
        newBus.setBusName(bus.busName());
        newBus.setBusDriver(getBusDriver);
        newBus.setMunicipality(municipality);

        for (UUID universityId : bus.universityIds()) {
            University university = universityRepository.findById(universityId)
                    .orElseThrow(() -> new ObjectNotFoundException("Universidade não encontrada."));
            universities.add(university);
        }

        newBus.setUniversities(universities);
        return busMapper.toResponse(busRepository.save(newBus));
    }

    public BusResponseDTO getBus(UUID busId, UUID municipalityUserId) {
        Bus bus = busRepository.findByBusIdAndMunicipality_User_UserId(busId, municipalityUserId)
                .orElseThrow(() -> new CrossMunicipalityAccessException("Esse ônibus não existe"));
        return busMapper.toResponse(bus);
    }

    public List<BusResponseDTO> getAllBusFromMunicipality(UUID municipalityUserId) {
        return busRepository
                .findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(busMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteBus(UUID busId, UUID municipalityUserId) {
        Bus bus = busRepository.findByBusIdAndMunicipality_User_UserId(busId, municipalityUserId)
                .orElseThrow(() -> new CrossMunicipalityAccessException("Esse ônibus não existe"));

        busRepository.delete(bus);
    }
}