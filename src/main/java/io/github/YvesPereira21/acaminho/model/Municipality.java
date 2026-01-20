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
    private String municipalityName;
    @OneToOne
    private User user;
    @OneToOne
    private City city;
    @OneToMany(mappedBy = "municipality", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Bus> fleet;
    @OneToMany(mappedBy = "municipality", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BusDriver> busDrivers;
    @OneToMany(mappedBy = "municipality")
    private List<UniversityStudent> universityStudents;

    public Municipality() {}

    public Municipality(UUID municipalityId, String municipalityName, User user, City city, List<Bus> fleet, List<BusDriver> busDrivers, List<UniversityStudent> universityStudents) {
        this.municipalityId = municipalityId;
        this.municipalityName = municipalityName;
        this.user = user;
        this.city = city;
        this.fleet = fleet;
        this.busDrivers = busDrivers;
        this.universityStudents = universityStudents;
    }

    public UUID getMunicipalityId() {
        return municipalityId;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
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

    public List<UniversityStudent> getUniversityStudents() {
        return universityStudents;
    }

    public void setUniversityStudents(List<UniversityStudent> universityStudents) {
        this.universityStudents = universityStudents;
    }
}