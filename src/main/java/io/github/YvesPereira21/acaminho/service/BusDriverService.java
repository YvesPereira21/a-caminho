package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.enums.UserRole;
import io.github.YvesPereira21.acaminho.mapper.BusDriverMapper;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BusDriverService {

    private final BusDriverRepository busDriverRepository;
    private final BusDriverMapper busDriverMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public BusDriverService(BusDriverRepository busDriverRepository, BusDriverMapper busDriverMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.busDriverRepository = busDriverRepository;
        this.busDriverMapper = busDriverMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public BusDriverResponseDTO createBusDriverAccount(BusDriverRequestDTO busDriverRequestDTO) {
        User newUser = new User();
        newUser.setEmail(busDriverRequestDTO.user().email());
        newUser.setPassword(bCryptPasswordEncoder.encode(busDriverRequestDTO.user().password()));
        newUser.setRole(UserRole.BUSDRIVER);

        BusDriver busDriver = new BusDriver();
        busDriver.setBusDriverName(busDriverRequestDTO.busDriverName());
        busDriver.setUser(newUser);
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