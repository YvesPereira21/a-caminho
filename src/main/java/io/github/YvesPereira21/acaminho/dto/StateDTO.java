package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;

public class StateDTO {

    @NotBlank
    private String stateName;

    public StateDTO() {
    }

    public StateDTO(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}