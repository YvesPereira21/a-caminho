package io.github.YvesPereira21.acaminho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UniversityStudentDTO {

    @NotBlank
    private String studentName;
    @NotNull
    @Valid
    private UserDTO user;
    @NotNull
    private UUID universityId;

    public UniversityStudentDTO() {
    }

    public UniversityStudentDTO(String studentName, UserDTO user, UUID universityId) {
        this.studentName = studentName;
        this.user = user;
        this.universityId = universityId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UUID getUniversityId() {
        return universityId;
    }

    public void setUniversityId(UUID universityId) {
        this.universityId = universityId;
    }
}