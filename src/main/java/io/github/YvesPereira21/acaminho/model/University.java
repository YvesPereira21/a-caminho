package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_university")
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "university_id")
    private UUID universityId;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "cityId")
    private City city;
    @OneToMany(mappedBy = "university")
    private List<UniversityStudent> students;
    @ManyToMany(mappedBy = "universities")
    private List<Bus> fleet;

    public University() {}

    public University(UUID universityId, String name, City city, List<UniversityStudent> students, List<Bus> fleet) {
        this.universityId = universityId;
        this.name = name;
        this.city = city;
        this.students = students;
        this.fleet = fleet;
    }

    public UUID getUniversityId() {
        return universityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<UniversityStudent> getStudents() {
        return students;
    }

    public void setStudents(List<UniversityStudent> students) {
        this.students = students;
    }

    public List<Bus> getFleet() {
        return fleet;
    }

    public void setFleet(List<Bus> fleet) {
        this.fleet = fleet;
    }
}