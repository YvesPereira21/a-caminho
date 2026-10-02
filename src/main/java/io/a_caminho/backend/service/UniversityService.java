package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.university.UniversityRequestDTO;
import io.a_caminho.backend.dto.university.UniversityResponseDTO;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import io.a_caminho.backend.mapper.UniversityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.CityRepository;
import io.a_caminho.backend.repository.UniversityRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final CityRepository cityRepository;
    private final UniversityMapper universityMapper;

    public UniversityService(
            UniversityRepository universityRepository,
            CityRepository cityRepository,
            UniversityMapper universityMapper
    ) {
        this.universityRepository = universityRepository;
        this.cityRepository = cityRepository;
        this.universityMapper = universityMapper;
    }

    @Transactional
    public UniversityResponseDTO createUniversity(UniversityRequestDTO university) {
        City city = cityRepository
                .findByCityNameAndState_StateName(
                        university.cityName(),
                        university.stateName()
                )
                .orElseThrow(() ->
                        new ObjectNotFoundException("Cidade não encontrada.")
                );

        University newUniversity = universityMapper.toEntity(university);
        newUniversity.setCity(city);

        return universityMapper.toResponse(
                universityRepository.save(newUniversity)
        );
    }

    @Transactional(readOnly = true)
    public List<UniversityResponseDTO> getAllUniversityByNameFromState(String universityName, String stateName) {
        return universityRepository
                .findAllByCampusContainingIgnoreCaseAndCity_State_StateName(universityName, stateName)
                .stream()
                .map(universityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUniversity(UUID universityId) {
        University university = universityRepository
                .findById(universityId)
                .orElseThrow(() ->
                        new ObjectNotFoundException("Universidade não encontrada.")
                );

        universityRepository.delete(university);
    }
}
