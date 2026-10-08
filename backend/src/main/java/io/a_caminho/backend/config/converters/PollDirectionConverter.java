package io.a_caminho.backend.config.converters;

import io.a_caminho.backend.model.enums.PollDirection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PollDirectionConverter implements AttributeConverter<PollDirection, String> {

    @Override
    public String convertToDatabaseColumn(PollDirection pollDirection) {
        return pollDirection != null ? pollDirection.getCode() : null;
    }

    @Override
    public PollDirection convertToEntityAttribute(String value) {
        return value != null ? PollDirection.fromValue(value) : null;
    }
}
