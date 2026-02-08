package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.UniversityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityResponseDTO;
import io.github.YvesPereira21.acaminho.mapper.UniversityMapper;
import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.repository.CityRepository;
import io.github.YvesPereira21.acaminho.repository.UniversityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final CityRepository cityRepository;
    private final UniversityMapper universityMapper;

    public UniversityService(UniversityRepository universityRepository, CityRepository cityRepository, UniversityMapper universityMapper) {
        this.universityRepository = universityRepository;
        this.cityRepository = cityRepository;
        this.universityMapper = universityMapper;
    }

    public UniversityResponseDTO createUniversity(UniversityRequestDTO university) {
        City city = cityRepository
                .findByCityNameAndState_StateName(university.cityName(), university.stateName())
                .orElseThrow();
        University newUniversity = new University();
        newUniversity.setName(university.name());
        newUniversity.setCity(city);
        return universityMapper.toResponse(universityRepository.save(newUniversity));
    }

    public List<UniversityResponseDTO> allUniversityContainingName(String universityName) {
        return universityRepository
                .findAllUniversityByNameContainingIgnoreCase(universityName)
                .stream()
                .map(universityMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteUniversity(UUID universityId) {
        University university = universityRepository
                .findById(universityId)
                .orElseThrow();
        universityRepository.deleteById(universityId);
    }
}