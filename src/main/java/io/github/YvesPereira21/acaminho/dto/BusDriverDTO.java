package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class BusDriverDTO {

    @NotBlank
    private String busDriverName;
    @NotNull
    @Valid
    private UserDTO user;
    @NotNull
    private UUID busId;
    @NotNull
    private UUID municipalityId;

    public BusDriverDTO() {}

    public BusDriverDTO(String busDriverName, UserDTO user, UUID busId, UUID municipalityId) {
        this.busDriverName = busDriverName;
        this.user = user;
        this.busId = busId;
        this.municipalityId = municipalityId;
    }

    public String getBusDriverName() {
        return busDriverName;
    }

    public void setBusDriverName(String busDriverName) {
        this.busDriverName = busDriverName;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UUID getBusId() {
        return busId;
    }

    public void setBusId(UUID busId) {
        this.busId = busId;
    }

    public UUID getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(UUID municipalityId) {
        this.municipalityId = municipalityId;
    }
}