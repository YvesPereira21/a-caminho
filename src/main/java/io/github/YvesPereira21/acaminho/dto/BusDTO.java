package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class BusDTO {
    @NotBlank
    private String busName;
    @NotNull
    private UUID municipalityId;

    public BusDTO() {}

    public BusDTO(String busName, UUID municipalityId) {
        this.busName = busName;
        this.municipalityId = municipalityId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public UUID getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(UUID municipalityId) {
        this.municipalityId = municipalityId;
    }
}