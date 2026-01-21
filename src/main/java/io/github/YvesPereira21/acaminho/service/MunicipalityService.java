package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import org.springframework.stereotype.Service;

@Service
public class MunicipalityService {
    private final MunicipalityRepository municipalityRepository;

    public MunicipalityService(MunicipalityRepository municipalityRepository) {
        this.municipalityRepository = municipalityRepository;
    }

    public Municipality createMunicipality(Municipality municipality) {
        return municipalityRepository.save(municipality);
    }

    public Municipality getMunicipalityByMunicipalityName(String municipalityName) {
        return municipalityRepository.findByMunicipalityName(municipalityName);
    }

    public Municipality updateMunicipality(String municipalityName, Municipality newMunicipality) {
        Municipality municipality = getMunicipalityByMunicipalityName(municipalityName);
        municipality = newMunicipality;
        return municipalityRepository.save(municipality);
    }

    public void deleteMunicipality(String cityName) {
        Municipality municipality = getMunicipalityByMunicipalityName(cityName);
        municipalityRepository.delete(municipality);
    }
}