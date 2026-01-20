package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public class CityDTO {

    @NotBlank
    private String cityName;
    @NotBlank
    private String stateName;

    public CityDTO() {
    }

    public CityDTO(String cityName, String stateName) {
        this.cityName = cityName;
        this.stateName = stateName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}