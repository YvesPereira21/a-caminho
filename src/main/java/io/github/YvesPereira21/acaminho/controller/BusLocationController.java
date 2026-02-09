package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.BusLocationDTO;
import io.github.YvesPereira21.acaminho.service.BusLocationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class BusLocationController {

    private final BusLocationService busLocationService;

    public BusLocationController(BusLocationService busLocationService) {
        this.busLocationService = busLocationService;
    }

    @MessageMapping("/location")
    public void handleLocationUpdate(@Payload BusLocationDTO busLocationDTO){
        busLocationService.updateLocation(
                busLocationDTO.busId(),
                busLocationDTO.latitude(),
                busLocationDTO.longitude()
        );
    }
}
