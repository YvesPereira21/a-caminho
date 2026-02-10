package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_state")
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "state_id")
    private UUID stateId;
    @Column(name = "state_name")
    private String stateName;
    @OneToMany(mappedBy = "state")
    private List<City> cities;

    public State() {
    }

    public State(UUID stateId, String stateName, List<City> cities) {
        this.stateId = stateId;
        this.stateName = stateName;
        this.cities = cities;
    }

    public UUID getStateId() {
        return stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }
}