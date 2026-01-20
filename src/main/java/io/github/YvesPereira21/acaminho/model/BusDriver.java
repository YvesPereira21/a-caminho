package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_bus_driver")
public class BusDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID busDriverId;
    @Column
    private String busDriverName;
    @OneToOne
    @JoinColumn(name = "userId")
    private User user;
    @OneToOne(mappedBy = "busDriver")
    private Bus bus;
    @ManyToOne
    @JoinColumn(name = "municipalityId")
    private Municipality municipality;

    public BusDriver() {
    }

    public BusDriver(UUID busDriverId, String busDriverName, User user, Bus bus, Municipality municipality) {
        this.busDriverId = busDriverId;
        this.busDriverName = busDriverName;
        this.user = user;
        this.bus = bus;
        this.municipality = municipality;
    }

    public UUID getBusDriverId() {
        return busDriverId;
    }

    public String getBusDriverName() {
        return busDriverName;
    }

    public void setBusDriverName(String busDriverName) {
        this.busDriverName = busDriverName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }
}