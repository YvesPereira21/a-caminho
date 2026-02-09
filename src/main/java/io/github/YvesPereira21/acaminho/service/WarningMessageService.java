package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.WarningMessageDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WarningMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WarningMessageService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendWarningMessage(String message, UUID municipalityId, UUID busId) {
        WarningMessageDTO payload = new WarningMessageDTO(message, municipalityId, busId);

        simpMessagingTemplate.convertAndSendToUser(
                municipalityId.toString(),
                "/queue/warnings",
                payload
        );
    }
}
