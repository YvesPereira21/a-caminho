package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.PollControllerOpenApi;
import io.a_caminho.backend.dto.poll.PollDTO;
import io.a_caminho.backend.dto.poll.PollListDTO;
import io.a_caminho.backend.service.PollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController implements PollControllerOpenApi {

    private final PollService pollService;

    @Override
    @PreAuthorize("hasAnyRole('MUNICIPALITY', 'STUDENT')")
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDTO> getPoll(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID pollId
    ) {
        log.info("Request to fetch poll ID: {} by user ID: {}", pollId, userId);
        PollDTO pollDTO = pollService.getPoll(userId, pollId);
        return ResponseEntity.ok(pollDTO);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping
    public ResponseEntity<List<PollListDTO>> getAllPollsFromMunicipality(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all polls for municipality user ID: {}", municipalityUserId);
        List<PollListDTO> polls = pollService.getAllPollFromMunicipality(municipalityUserId);
        return ResponseEntity.ok(polls);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/open")
    public ResponseEntity<List<PollListDTO>> getOpenPollsToStudent(
            @AuthenticationPrincipal(expression = "id") UUID studentUserId
    ) {
        log.info("Request to fetch open polls for student user ID: {}", studentUserId);
        List<PollListDTO> polls = pollService.getOpenPollsToStudent(studentUserId);
        return ResponseEntity.ok(polls);
    }
}
