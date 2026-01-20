package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "tb_city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID cityId;
    @Column
    private String cityName;
    @Column
    private String state;

    public City() {
    }

    public City(UUID cityId, String cityName, String state) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.state = state;
    }

    public UUID getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}