package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.CityRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.CityResponseDTO;
import io.github.YvesPereira21.acaminho.mapper.CityMapper;
import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.model.State;
import io.github.YvesPereira21.acaminho.repository.CityRepository;
import io.github.YvesPereira21.acaminho.repository.StateRepository;
import org.springframework.stereotype.Service;

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
                .orElseThrow();
        City newCity = new City();
        newCity.setState(state);
        return cityMapper.toResponse(cityRepository.save(newCity));
    }

    public CityResponseDTO getCityByCityNameAndStateName(String cityName, String stateName) {
        City city = cityRepository.findByCityNameAndState_StateName(cityName, stateName)
                .orElseThrow();
        return cityMapper.toResponse(city);
    }

    public void deleteCity(String cityName, String stateName) {
        City city = cityRepository.findByCityNameAndState_StateName(cityName, stateName)
                .orElseThrow();
        cityRepository.delete(city);
    }
}