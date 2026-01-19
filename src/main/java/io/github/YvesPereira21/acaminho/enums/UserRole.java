package io.github.YvesPereira21.acaminho.enums;

public enum UserRole {
    ADMIN("admin"),
    UNIVERSITYSTUDENT("university student"),
    CITYHALL("city hall");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
}
