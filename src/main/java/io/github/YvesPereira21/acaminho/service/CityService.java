package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.City;
import io.github.YvesPereira21.acaminho.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City createCity(City city) {
        return cityRepository.save(city);
    }

    public City getCityByCityNameAndStateName(String cityName, String stateName) {
        return cityRepository.findByCityNameAndState_StateName(cityName, stateName)
                .orElseThrow();
    }

    public void deleteCity(UUID cityId) {
        City city = cityRepository.findById(cityId).orElseThrow();
        cityRepository.delete(city);
    }
}