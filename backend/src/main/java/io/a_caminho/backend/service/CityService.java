package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.city.CityRequestDTO;
import io.a_caminho.backend.dto.city.CityResponseDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.mapper.CityMapper;
import io.a_caminho.backend.model.City;
import io.a_caminho.backend.model.State;
import io.a_caminho.backend.repository.CityRepository;
import io.a_caminho.backend.repository.StateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CityMapper cityMapper;

    public CityService(
            CityRepository cityRepository,
            StateRepository stateRepository,
            CityMapper cityMapper
    ) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
        this.cityMapper = cityMapper;
    }

    @Transactional
    public CityResponseDTO createCity(CityRequestDTO cityDTO) {
        boolean cityExists = cityRepository.existsByCityNameAndState_StateName(cityDTO.cityName(), cityDTO.stateName());
        if (cityExists) {
            throw new ObjectAlreadyExistsException("Cidade já existe.");
        }

        State state = stateRepository.findByStateName(cityDTO.stateName())
                .orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado."));

        City newCity = cityMapper.toEntity(cityDTO);
        newCity.setState(state);

        return cityMapper.toResponse(cityRepository.save(newCity));
    }

    @Transactional(readOnly = true)
    public CityResponseDTO getCityById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Cidade não encontrada"));

        return cityMapper.toResponse(city);
    }

    @Transactional(readOnly = true)
    public List<CityResponseDTO> getAllCityFromStateByStateName(String stateName) {
        State state = stateRepository.findByStateName(stateName)
                .orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado."));

        return cityRepository.findAllByState_StateName(stateName)
                .stream()
                .map(cityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCity(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Cidade não encontrada"));

        cityRepository.delete(city);
    }
}
