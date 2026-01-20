package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class MunicipalityDTO {
    @NotBlank
    private String municipalityName;
    @NotNull
    @Valid
    private UserDTO user;
    @NotNull
    private UUID cityId;

    public MunicipalityDTO() {
    }

    public MunicipalityDTO(String municipalityName, UserDTO user, UUID cityId) {
        this.municipalityName = municipalityName;
        this.user = user;
        this.cityId = cityId;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }
}