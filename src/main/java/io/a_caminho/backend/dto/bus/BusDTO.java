package io.a_caminho.backend.dto.bus;

import java.util.UUID;

public record BusDTO(
        UUID busId,
        String busName,
        Integer seatsQuantity
) {}
