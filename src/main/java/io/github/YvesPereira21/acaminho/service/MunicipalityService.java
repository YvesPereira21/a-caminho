package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.MunicipalityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.MunicipalityResponseDTO;
import io.github.YvesPereira21.acaminho.enums.UserRole;
import io.github.YvesPereira21.acaminho.mapper.MunicipalityMapper;
import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.repository.CityRepository;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import org.springframework.stereotype.Service;

@Service
public class MunicipalityService {

    private final MunicipalityRepository municipalityRepository;
    private final CityRepository cityRepository;
    private final MunicipalityMapper municipalityMapper;

    public MunicipalityService(MunicipalityRepository municipalityRepository, CityRepository cityRepository, MunicipalityMapper municipalityMapper) {
        this.municipalityRepository = municipalityRepository;
        this.cityRepository = cityRepository;
        this.municipalityMapper = municipalityMapper;
    }

    public MunicipalityResponseDTO createMunicipality(MunicipalityRequestDTO municipality) {
        City city = cityRepository.findById(municipality.cityId())
                .orElseThrow();
        User newUser = new User();
        newUser.setEmail(municipality.user().email());
        newUser.setPassword(municipality.user().password());
        newUser.setRole(UserRole.MUNICIPALITY);

        Municipality newMunicipality = new Municipality();
        newMunicipality.setMunicipalityName(municipality.municipalityName());
        newMunicipality.setCity(city);
        newMunicipality.setUser(newUser);

        return municipalityMapper.toResponse(municipalityRepository.save(newMunicipality));
    }

    public MunicipalityResponseDTO getMunicipalityByName(String municipalityName) {
        Municipality municipality = municipalityRepository.findByMunicipalityName(municipalityName);
        return municipalityMapper.toResponse(municipality);
    }

    public void deleteMunicipality(String municipalityName) {
        Municipality municipality = municipalityRepository.findByMunicipalityName(municipalityName);
        municipalityRepository.delete(municipality);
    }
}