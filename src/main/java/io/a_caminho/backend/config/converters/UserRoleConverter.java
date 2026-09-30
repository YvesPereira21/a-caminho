package io.a_caminho.backend.config.converters;

import io.a_caminho.backend.model.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole userRole) {
        return userRole != null ? userRole.getCode() : null;
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        return value != null ? UserRole.fromValue(value) : null;
    }
}
