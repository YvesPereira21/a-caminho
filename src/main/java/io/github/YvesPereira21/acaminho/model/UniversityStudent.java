package io.github.YvesPereira21.acaminho.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_university_student")
public class UniversityStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID universityStudentId;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User user;
    @ManyToOne
    @JoinColumn(name = "universityId")
    private University university;

    public UniversityStudent() {}

    public UniversityStudent(UUID universityStudentId, User user, University university) {
        this.universityStudentId = universityStudentId;
        this.user = user;
        this.university = university;
    }

    public UUID getUniversityStudentId() {
        return universityStudentId;
    }

    public void setUniversityStudentId(UUID universityStudentId) {
        this.universityStudentId = universityStudentId;
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