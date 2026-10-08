package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.PollVoteControllerOpenApi;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.pollvote.VoteRequestDTO;
import io.a_caminho.backend.dto.pollvote.VoteResponseDTO;
import io.a_caminho.backend.service.PollVoteService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/polls/{pollId}")
@RequiredArgsConstructor
public class PollVoteController implements PollVoteControllerOpenApi {

    private final PollVoteService pollVoteService;

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/votes")
    public ResponseEntity<Void> vote(
            @AuthenticationPrincipal(expression = "id") UUID studentUserId,
            @PathVariable UUID pollId,
            @Valid @RequestBody VoteRequestDTO voteRequestDTO
    ) {
        log.info("Request to vote on poll ID: {} by student user ID: {}", pollId, studentUserId);
        pollVoteService.vote(studentUserId, pollId, voteRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-vote")
    public ResponseEntity<VoteResponseDTO> getMyVote(
            @AuthenticationPrincipal(expression = "id") UUID studentUserId,
            @PathVariable UUID pollId
    ) {
        log.info("Request to fetch own vote on poll ID: {} by student user ID: {}", pollId, studentUserId);
        VoteResponseDTO voteResponseDTO = pollVoteService.getMyVote(studentUserId, pollId);
        return ResponseEntity.ok(voteResponseDTO);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/passengers")
    public ResponseEntity<List<PollPassengerDTO>> getPollPassengers(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID pollId
    ) {
        log.info("Request to fetch passengers for poll ID: {} by municipality user ID: {}", pollId, municipalityUserId);
        List<PollPassengerDTO> passengers = pollVoteService.getPollPassengers(municipalityUserId, pollId);
        return ResponseEntity.ok(passengers);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/votes")
    public ResponseEntity<Void> cancelVote(
            @AuthenticationPrincipal(expression = "id") UUID studentUserId,
            @PathVariable UUID pollId
    ) {
        log.info("Request to cancel vote on poll ID: {} by student user ID: {}", pollId, studentUserId);
        pollVoteService.cancelVote(studentUserId, pollId);
        return ResponseEntity.noContent().build();
    }
}
