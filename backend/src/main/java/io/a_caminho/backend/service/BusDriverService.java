package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.busdriver.BusDriverRequestDTO;
import io.a_caminho.backend.dto.busdriver.BusDriverResponseDTO;
import io.a_caminho.backend.exception.*;
import io.a_caminho.backend.mapper.BusDriverMapper;
import io.a_caminho.backend.model.BusDriver;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.BusDriverRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusDriverService {

    private final BusDriverRepository busDriverRepository;
    private final MunicipalityRepository municipalityRepository;
    private final BusDriverMapper busDriverMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BusDriverService(
            BusDriverRepository busDriverRepository,
            MunicipalityRepository municipalityRepository,
            BusDriverMapper busDriverMapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.busDriverRepository = busDriverRepository;
        this.municipalityRepository = municipalityRepository;
        this.busDriverMapper = busDriverMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public BusDriverResponseDTO createBusDriverAccount(
            BusDriverRequestDTO busDriver,
            UUID municipalityUserId
    ) {
        Municipality municipality = municipalityRepository
                .findByUser_UserId(municipalityUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não existe."));

        boolean userExists = verifyUserAlreadyExists(busDriver.user().email());
        if(userExists) {
            throw new ObjectAlreadyExistsException("Usuário já existente.");
        }

        User newUser = new User();
        newUser.setEmail(busDriver.user().email());
        newUser.setPassword(
                passwordEncoder.encode(busDriver.user().password())
        );
        newUser.setRole(UserRole.BUS_DRIVER);

        BusDriver newBusDriver = busDriverMapper.toEntity(busDriver);
        newBusDriver.setUser(newUser);
        newBusDriver.setMunicipality(municipality);

        return busDriverMapper.toResponse(busDriverRepository.save(newBusDriver));
    }

    @Transactional(readOnly = true)
    public BusDriverResponseDTO getBusDriverById(
            UUID busDriverId,
            UUID municipalityUserId
    ) {
        BusDriver busDriver = busDriverRepository
                .findByBusDriverIdAndMunicipality_User_UserId(
                        busDriverId,
                        municipalityUserId
                )
                .orElseThrow(() ->
                        new CrossMunicipalityAccessException("Motorista não encontrado.")
                );

        return busDriverMapper.toResponse(busDriver);
    }

    @Transactional(readOnly = true)
    public List<BusDriverResponseDTO> getAllBusDriversFromMunicipality(
            UUID municipalityUserId
    ) {
        return busDriverRepository
                .findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(busDriverMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBusDriverAccount(UUID busDriverId, UUID authenticatedUserId) {
        BusDriver busDriver = busDriverRepository
                .findByBusDriverIdAndMunicipality_User_UserId(
                        busDriverId,
                        authenticatedUserId
                )
                .orElseThrow(() ->
                        new CrossMunicipalityAccessException("Motorista não encontrado.")
                );

        Municipality municipality = busDriver.getMunicipality();
        if (!municipality.getUser().getId().equals(authenticatedUserId)) {
            throw new UserIsNotOwnerException("Você não tem permissão para isso");
        }

        busDriverRepository.delete(busDriver);
    }

    private boolean verifyUserAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
