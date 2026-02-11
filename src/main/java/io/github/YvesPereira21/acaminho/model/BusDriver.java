package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_bus_driver")
public class BusDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bus_driver_id")
    private UUID busDriverId;
    @Column(name = "bus_driver_name")
    private String busDriverName;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne(mappedBy = "busDriver")
    private Bus bus;
    @ManyToOne
    @JoinColumn(name = "municipality_id")
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