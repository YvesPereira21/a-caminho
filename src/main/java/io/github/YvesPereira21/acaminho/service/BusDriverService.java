package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.BusDriverRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.BusDriverResponseDTO;
import io.github.YvesPereira21.acaminho.enums.UserRole;
import io.github.YvesPereira21.acaminho.exception.CrossMunicipalityAccessException;
import io.github.YvesPereira21.acaminho.exception.ObjectNotFoundException;
import io.github.YvesPereira21.acaminho.mapper.BusDriverMapper;
import io.github.YvesPereira21.acaminho.model.BusDriver;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.repository.BusDriverRepository;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusDriverService {

    private final BusDriverRepository busDriverRepository;
    private final MunicipalityRepository municipalityRepository;
    private final BusDriverMapper busDriverMapper;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public BusDriverService(BusDriverRepository busDriverRepository, MunicipalityRepository municipalityRepository, BusDriverMapper busDriverMapper, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.busDriverRepository = busDriverRepository;
        this.municipalityRepository = municipalityRepository;
        this.busDriverMapper = busDriverMapper;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public BusDriverResponseDTO createBusDriverAccount(BusDriverRequestDTO busDriver, UUID municipalityUserId) {
        Municipality municipality = municipalityRepository
                .findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));

        userService.verifyUserAlreadyExists(busDriver.user().email());

        User newUser = new User();
        newUser.setEmail(busDriver.user().email());
        newUser.setPassword(bCryptPasswordEncoder.encode(busDriver.user().password()));
        newUser.setRole(UserRole.BUSDRIVER);

        BusDriver newBusDriver = new BusDriver();
        newBusDriver.setBusDriverName(busDriver.busDriverName());
        newBusDriver.setUser(newUser);
        newBusDriver.setMunicipality(municipality);
        return busDriverMapper.toResponse(busDriverRepository.save(newBusDriver));
    }

    public BusDriverResponseDTO getBusDriverById(UUID busDriverId, UUID municipalityUserId) {
        BusDriver busDriver = busDriverRepository
                .findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId)
                .orElseThrow(() -> new CrossMunicipalityAccessException("Motorista não encontrado."));

        return busDriverMapper.toResponse(busDriver);
    }

    public List<BusDriverResponseDTO> getAllBusDriversFromMunicipality(UUID municipalityUserId) {
        return busDriverRepository.findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(busDriverMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteBusDriverAccount(UUID busDriverId, UUID municipalityUserId) {
        BusDriver busDriver = busDriverRepository
                .findByBusDriverIdAndMunicipality_User_UserId(busDriverId, municipalityUserId)
                .orElseThrow(() -> new CrossMunicipalityAccessException("Motorista não encontrado."));

        busDriverRepository.delete(busDriver);
    }
}