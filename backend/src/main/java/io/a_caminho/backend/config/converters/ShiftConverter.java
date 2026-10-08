package io.a_caminho.backend.config.converters;

import io.a_caminho.backend.model.enums.Shift;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShiftConverter implements AttributeConverter<Shift, String> {

    @Override
    public String convertToDatabaseColumn(Shift shift) {
        return shift != null ? shift.getCode() : null;
    }

    @Override
    public Shift convertToEntityAttribute(String value) {
        return value != null ? Shift.fromValue(value) : null;
    }
}
