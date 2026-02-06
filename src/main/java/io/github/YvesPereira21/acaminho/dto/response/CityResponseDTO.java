package io.github.YvesPereira21.acaminho.dto.response;

public record CityResponseDTO(
        String stateName,
        String cityName,
        MunicipalityResponseDTO municipality
){}
