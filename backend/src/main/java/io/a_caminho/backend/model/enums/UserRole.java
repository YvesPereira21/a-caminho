package io.a_caminho.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    ADMIN("ADMINISTRADOR", "Administrador"),
    MUNICIPALITY("PREFEITURA", "Prefeitura"),
    BUS_DRIVER("MOTORISTA", "Motorista"),
    STUDENT("ESTUDANTE", "Estudante");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    @JsonValue
    public String getDescription() {
        return this.description;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole userRole : values()) {
            if (userRole.code.equalsIgnoreCase(value) ||
                    userRole.description.equalsIgnoreCase(value) ||
                    userRole.name().equalsIgnoreCase(value)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
