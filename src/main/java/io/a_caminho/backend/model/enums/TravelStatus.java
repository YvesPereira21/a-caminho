package io.a_caminho.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TravelStatus {
    SCHEDULED("AGENDADA", "Agendada"),
    IN_PROGRESS("EM_ANDAMENTO", "Em andamento"),
    COMPLETED("CONCLUIDA", "Concluída"),
    CANCELLED("CANCELADA", "Cancelada");

    private final String code;
    private final String description;

    TravelStatus(String code, String description) {
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
    public static TravelStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TravelStatus travelStatus : values()) {
            if (travelStatus.code.equalsIgnoreCase(value)
                    || travelStatus.description.equalsIgnoreCase(value)
                    || travelStatus.name().equalsIgnoreCase(value)) {
                return travelStatus;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
