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
    private String universityName;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User user;
    @ManyToOne
    @JoinColumn(name = "universityId")
    private University university;

    public UniversityStudent() {}

    public UniversityStudent(UUID universityStudentId, String universityName, User user, University university) {
        this.universityStudentId = universityStudentId;
        this.universityName = universityName;
        this.user = user;
        this.university = university;
    }

    public UUID getUniversityStudentId() {
        return universityStudentId;
    }

    public void setUniversityStudentId(UUID universityStudentId) {
        this.universityStudentId = universityStudentId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }
}