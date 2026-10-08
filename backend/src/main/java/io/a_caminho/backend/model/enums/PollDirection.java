package io.a_caminho.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PollDirection {
    OUTBOUND("IDA", "Ida"),
    INBOUND("VOLTA", "Volta");

    private final String code;
    private final String description;

    PollDirection(String code, String description) {
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
    public static PollDirection fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PollDirection pollDirection : values()) {
            if (pollDirection.code.equalsIgnoreCase(value) ||
                    pollDirection.description.equalsIgnoreCase(value) ||
                    pollDirection.name().equalsIgnoreCase(value)) {
                return pollDirection;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
