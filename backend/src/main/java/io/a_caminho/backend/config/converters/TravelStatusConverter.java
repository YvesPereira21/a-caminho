package io.a_caminho.backend.config.converters;

import io.a_caminho.backend.model.enums.TravelStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TravelStatusConverter implements AttributeConverter<TravelStatus, String> {

    @Override
    public String convertToDatabaseColumn(TravelStatus travelStatus) {
        return travelStatus != null ? travelStatus.getCode() : null;
    }

    @Override
    public TravelStatus convertToEntityAttribute(String value) {
        return value != null ? TravelStatus.fromValue(value) : null;
    }
}
