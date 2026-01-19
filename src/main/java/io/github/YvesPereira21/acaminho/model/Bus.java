package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "tb_bus")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID busId;
    @Column
    private String busName;
    @ManyToOne
    @JoinColumn(name = "municipalityId")
    private Municipality municipality;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "busDriverId")
    private BusDriver busDriver;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="route_bus",
            joinColumns = @JoinColumn(name = "busId"),
            inverseJoinColumns = @JoinColumn(name = "universityId")
    )
    private List<University> universities;

    public Bus() {}

    public Bus(UUID busId, String busName, Municipality municipality, BusDriver busDriver, List<University> universities) {
        this.busId = busId;
        this.busName = busName;
        this.municipality = municipality;
        this.busDriver = busDriver;
        this.universities = universities;
    }

    public UUID getBusId() {
        return busId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public BusDriver getBusDriver() {
        return busDriver;
    }

    public void setBusDriver(BusDriver busDriver) {
        this.busDriver = busDriver;
    }

    public List<University> getUniversities() {
        return universities;
    }

    public void setUniversities(List<University> universities) {
        this.universities = universities;
    }
}