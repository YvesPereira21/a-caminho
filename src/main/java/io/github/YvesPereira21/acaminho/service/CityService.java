package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.CityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.CityResponseDTO;
import io.github.YvesPereira21.acaminho.exception.ObjectNotFoundException;
import io.github.YvesPereira21.acaminho.mapper.CityMapper;
import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.model.State;
import io.github.YvesPereira21.acaminho.repository.CityRepository;
import io.github.YvesPereira21.acaminho.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, StateRepository stateRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
        this.cityMapper = cityMapper;
    }

    public CityResponseDTO createCity(CityRequestDTO city) {
        State state = stateRepository.findByStateName(city.stateName())
                .orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado."));

        City newCity = new City();
        newCity.setCityName(city.cityName());
        newCity.setState(state);

        return cityMapper.toResponse(cityRepository.save(newCity));
    }

    public CityResponseDTO getCityById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Cidade não encontrada"));
        return cityMapper.toResponse(city);
    }

    public List<CityResponseDTO> getAllCityFromStateByStateName(String stateName) {
        return cityRepository.findAllByState_StateName(stateName)
                .stream()
                .map(cityMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteCity(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado."));
        cityRepository.delete(city);
    }
}