package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.municipality.MunicipalityRequestDTO;
import io.a_caminho.backend.dto.municipality.MunicipalityResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerAndAdminException;
import io.a_caminho.backend.mapper.MunicipalityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.CityRepository;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MunicipalityService {

    private final MunicipalityRepository municipalityRepository;
    private final CityRepository cityRepository;
    private final MunicipalityMapper municipalityMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MunicipalityService(
            MunicipalityRepository municipalityRepository,
            CityRepository cityRepository,
            MunicipalityMapper municipalityMapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.municipalityRepository = municipalityRepository;
        this.cityRepository = cityRepository;
        this.municipalityMapper = municipalityMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MunicipalityResponseDTO createMunicipality(MunicipalityRequestDTO municipality) {
        City city = cityRepository.findById(municipality.cityId())
                .orElseThrow(() -> new ObjectNotFoundException("Cidade não encontrada."));

        boolean userExists = verifyUserAlreadyExists(municipality.user().email(), municipality.municipalityName());
        if(userExists) {
            throw new ObjectAlreadyExistsException("Usuário já existente.");
        }

        User newUser = new User();
        newUser.setEmail(municipality.user().email());
        newUser.setPassword(passwordEncoder.encode(municipality.user().password()));
        newUser.setRole(UserRole.MUNICIPALITY);

        Municipality newMunicipality = municipalityMapper.toEntity(municipality);
        newMunicipality.setCity(city);
        newMunicipality.setUser(newUser);

        return municipalityMapper.toResponse(municipalityRepository.save(newMunicipality));
    }

    @Transactional(readOnly = true)
    public MunicipalityResponseDTO getMunicipalityByName(String municipalityName) {
        Municipality municipality = municipalityRepository
                .findByMunicipalityName(municipalityName)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        return municipalityMapper.toResponse(municipality);
    }

    @Transactional(readOnly = true)
    public List<MunicipalityResponseDTO> getAllMunicipalityFromState(String stateName) {
        return municipalityRepository
                .findAllMunicipalityFromStateByStateName(stateName)
                .stream()
                .map(municipalityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMunicipality(UUID authenticatedUserId, String municipalityName) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não existe"));
        Municipality municipality = municipalityRepository
                .findByMunicipalityName(municipalityName)
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));

        if (!municipality.getUser().getId().equals(authenticatedUserId)
                && !user.getRole().equals(UserRole.ADMIN)) {
            throw new UserIsNotOwnerAndAdminException("Você não tem permissão para isso");
        }

        municipalityRepository.delete(municipality);
    }

    private boolean verifyUserAlreadyExists(String email, String municipalityName) {
        if (userRepository.existsByEmail(email)) {
            return true;
        }
        if (municipalityRepository.existsByMunicipalityName(municipalityName)) {
            return true;
        }
        return false;
    }
}
