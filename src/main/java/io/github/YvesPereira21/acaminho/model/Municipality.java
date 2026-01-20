package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_municipality")
public class Municipality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID municipalityId;
    @Column(unique = true)
    private String cityName;
    @OneToMany(mappedBy = "municipality", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Bus> fleet;
    @OneToMany(mappedBy = "municipality", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BusDriver> busDrivers;

    public Municipality() {}

    public Municipality(UUID municipalityId, String cityName, List<Bus> fleet, List<BusDriver> busDrivers) {
        this.municipalityId = municipalityId;
        this.cityName = cityName;
        this.fleet = fleet;
        this.busDrivers = busDrivers;
    }

    public UUID getMunicipalityId() {
        return municipalityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<Bus> getFleet() {
        return fleet;
    }

    public void setFleet(List<Bus> fleet) {
        this.fleet = fleet;
    }

    public List<BusDriver> getBusDrivers() {
        return busDrivers;
    }

    public void setBusDrivers(List<BusDriver> busDrivers) {
        this.busDrivers = busDrivers;
    }
}