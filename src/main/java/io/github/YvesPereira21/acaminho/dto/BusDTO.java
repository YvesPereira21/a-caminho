package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class BusDTO {
    @NotBlank
    private String busName;
    @NotNull
    private UUID municipalityId;
    @NotNull
    @Valid
    private List<UniversityDTO> universities;

    public BusDTO() {}

    public BusDTO(String busName, UUID municipalityId, List<UniversityDTO> universities) {
        this.busName = busName;
        this.municipalityId = municipalityId;
        this.universities = universities;
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

    public List<UniversityDTO> getUniversities() {
        return universities;
    }

    public void setUniversities(List<UniversityDTO> universities) {
        this.universities = universities;
    }
}