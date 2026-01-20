package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_university_student")
public class UniversityStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID universityStudentId;
    @Column
    private String studentName;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User user;
    @ManyToOne
    @JoinColumn(name = "municipalityId")
    private Municipality municipality;
    @ManyToOne
    @JoinColumn(name = "universityId")
    private University university;

    public UniversityStudent() {}

    public UniversityStudent(UUID universityStudentId, User user, String studentName, Municipality municipality, University university) {
        this.universityStudentId = universityStudentId;
        this.user = user;
        this.studentName = studentName;
        this.municipality = municipality;
        this.university = university;
    }

    public UUID getUniversityStudentId() {
        return universityStudentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }
}