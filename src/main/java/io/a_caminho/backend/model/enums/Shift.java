package io.a_caminho.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Shift {
    MORNING("MANHA", "Manhã"),
    AFTERNOON("TARDE", "Tarde"),
    NIGHT("NOITE", "Noite");

    private final String code;
    private final String description;

    Shift(String code, String description) {
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
    public static Shift fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Shift shift : values()) {
            if (shift.code.equalsIgnoreCase(value) ||
                    shift.description.equalsIgnoreCase(value) ||
                    shift.name().equalsIgnoreCase(value)) {
                return shift;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
