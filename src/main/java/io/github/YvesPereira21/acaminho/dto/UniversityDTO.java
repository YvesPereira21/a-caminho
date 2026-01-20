package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public class UniversityDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String cityName;

    public UniversityDTO() {
    }

    public UniversityDTO(String name, String cityName) {
        this.name = name;
        this.cityName = cityName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}