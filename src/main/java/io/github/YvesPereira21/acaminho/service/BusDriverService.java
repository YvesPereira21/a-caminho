package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.enums.UserRole;
import io.github.YvesPereira21.acaminho.mapper.BusDriverMapper;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BusDriverService {

    private final BusDriverRepository busDriverRepository;
    private final MunicipalityRepository municipalityRepository;
    private final BusDriverMapper busDriverMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public BusDriverService(BusDriverRepository busDriverRepository, MunicipalityRepository municipalityRepository, BusDriverMapper busDriverMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.busDriverRepository = busDriverRepository;
        this.municipalityRepository = municipalityRepository;
        this.busDriverMapper = busDriverMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public BusDriverResponseDTO createBusDriverAccount(BusDriverRequestDTO busDriverRequestDTO, UUID municipalityUserId) {
        Municipality municipality = municipalityRepository
                .findByUser_UserId(municipalityUserId)
                .orElseThrow();

        User newUser = new User();
        newUser.setEmail(busDriverRequestDTO.user().email());
        newUser.setPassword(bCryptPasswordEncoder.encode(busDriverRequestDTO.user().password()));
        newUser.setRole(UserRole.BUSDRIVER);

        BusDriver busDriver = new BusDriver();
        busDriver.setBusDriverName(busDriverRequestDTO.busDriverName());
        busDriver.setUser(newUser);
        busDriver.setMunicipality(municipality);
        return busDriverMapper.toResponse(busDriverRepository.save(busDriver));
    }

    public BusDriverResponseDTO getBusDriverById(UUID busDriverId) {
        BusDriver busDriver = busDriverRepository.findByBusDriverId(busDriverId)
                .orElseThrow();
        return busDriverMapper.toResponse(busDriver);
    }

    public void deleteBusDriverAccount(UUID busDriverId) {
        BusDriver busDriver = busDriverRepository.findByBusDriverId(busDriverId)
                        .orElseThrow();
        busDriverRepository.delete(busDriver);
    }
}