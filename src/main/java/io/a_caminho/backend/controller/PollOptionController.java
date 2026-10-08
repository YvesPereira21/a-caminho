package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.PollOptionControllerOpenApi;
import io.a_caminho.backend.dto.polloption.PollOptionDTO;
import io.a_caminho.backend.dto.polloption.PollOptionResponseDTO;
import io.a_caminho.backend.service.PollOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/poll-options")
@RequiredArgsConstructor
public class PollOptionController implements PollOptionControllerOpenApi {

    private final PollOptionService pollOptionService;

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PostMapping
    public ResponseEntity<PollOptionResponseDTO> createPollOption(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody PollOptionDTO pollOptionDTO
    ) {
        log.info("Request to create poll option '{}' by municipality user ID: {}", pollOptionDTO.stopName(), municipalityUserId);
        PollOptionResponseDTO created = pollOptionService.createPollOption(municipalityUserId, pollOptionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping
    public ResponseEntity<List<PollOptionResponseDTO>> getAllPollOptions(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all poll options for municipality user ID: {}", municipalityUserId);
        List<PollOptionResponseDTO> options = pollOptionService.getAllPollOptions(municipalityUserId);
        return ResponseEntity.ok(options);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/{optionId}")
    public ResponseEntity<PollOptionResponseDTO> getPollOption(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID optionId
    ) {
        log.info("Request to fetch poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        PollOptionResponseDTO option = pollOptionService.getPollOption(municipalityUserId, optionId);
        return ResponseEntity.ok(option);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PutMapping("/{optionId}")
    public ResponseEntity<Void> updatePollOption(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID optionId,
            @Valid @RequestBody PollOptionDTO pollOptionDTO
    ) {
        log.info("Request to update poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        pollOptionService.updatePollOption(municipalityUserId, optionId, pollOptionDTO);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deletePollOption(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID optionId
    ) {
        log.info("Request to delete poll option ID: {} by municipality user ID: {}", optionId, municipalityUserId);
        pollOptionService.deleteOption(municipalityUserId, optionId);
        return ResponseEntity.noContent().build();
    }
}
