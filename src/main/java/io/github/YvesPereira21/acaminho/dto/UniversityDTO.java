package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public class UniversityDTO {

    @NotBlank
    private String name;

    public UniversityDTO() {
    }

    public UniversityDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}