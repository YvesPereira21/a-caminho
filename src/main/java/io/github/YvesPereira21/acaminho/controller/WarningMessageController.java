package io.github.YvesPereira21.acaminho.controller;

import io.github.YvesPereira21.acaminho.dto.WarningMessageDTO;
import io.github.YvesPereira21.acaminho.service.WarningMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WarningMessageController {

    private final WarningMessageService warningMessageService;

    public WarningMessageController(WarningMessageService warningMessageService) {
        this.warningMessageService = warningMessageService;
    }

    @MessageMapping("/warning")
    public void handleMessageWarning(@Payload WarningMessageDTO warningMessageDTO){
        warningMessageService.sendWarningMessage(
                warningMessageDTO.message(),
                warningMessageDTO.municipalityId(),
                warningMessageDTO.busId()
        );
    }
}
