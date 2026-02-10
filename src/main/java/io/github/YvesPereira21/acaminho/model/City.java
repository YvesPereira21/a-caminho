package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "city_id")
    private UUID cityId;
    @Column(name = "city_name")
    private String cityName;
    @OneToOne(mappedBy = "city")
    private Municipality municipality;
    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;
    @OneToMany(mappedBy = "city")
    private List<University> universities;

    public City() {
    }

    public City(UUID cityId, String cityName, Municipality municipality, State state, List<University> universities) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.municipality = municipality;
        this.state = state;
        this.universities = universities;
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

    public List<University> getUniversities() {
        return universities;
    }

    public void setUniversity(List<University> universities) {
        this.universities = universities;
    }
}