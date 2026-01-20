package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID cityId;
    @Column
    private String cityName;
    @OneToOne(mappedBy = "city")
    private Municipality municipality;
    @ManyToOne
    private State state;
    @OneToMany(mappedBy = "city")
    private List<University> university;

    public City() {
    }

    public City(UUID cityId, String cityName, Municipality municipality, State state, List<University> university) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.municipality = municipality;
        this.state = state;
        this.university = university;
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

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<University> getUniversity() {
        return university;
    }

    public void setUniversity(List<University> university) {
        this.university = university;
    }
}