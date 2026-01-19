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

    public BusDriver() {
    }

    public BusDriver(String busDriverName, User user) {
        this.busDriverName = busDriverName;
        this.user = user;
    }

    public UUID getBusDriverId() {
        return busDriverId;
    }

    public void setBusDriverId(UUID busDriverId) {
        this.busDriverId = busDriverId;
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
}