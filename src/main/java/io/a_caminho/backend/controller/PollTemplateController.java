package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.PollTemplateControllerOpenApi;
import io.a_caminho.backend.dto.polltemplate.PollTemplateCreateDTO;
import io.a_caminho.backend.dto.polltemplate.PollTemplateDTO;
import io.a_caminho.backend.service.PollTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/poll-templates")
@RequiredArgsConstructor
public class PollTemplateController implements PollTemplateControllerOpenApi {

    private final PollTemplateService pollTemplateService;

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PostMapping
    public ResponseEntity<PollTemplateDTO> createPollTemplate(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @Valid @RequestBody PollTemplateCreateDTO pollTemplateCreateDTO
    ) {
        log.info("Request to create poll template '{}' by municipality user ID: {}", pollTemplateCreateDTO.routeName(), municipalityUserId);
        PollTemplateDTO created = pollTemplateService.createPollTemplate(municipalityUserId, pollTemplateCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping
    public ResponseEntity<List<PollTemplateDTO>> getAllPollTemplates(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all poll templates for municipality user ID: {}", municipalityUserId);
        List<PollTemplateDTO> templates = pollTemplateService.getAllPollTemplateFromMunicipality(municipalityUserId);
        return ResponseEntity.ok(templates);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/{templateId}")
    public ResponseEntity<PollTemplateDTO> getPollTemplate(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID templateId
    ) {
        log.info("Request to fetch poll template ID: {} by municipality user ID: {}", templateId, municipalityUserId);
        PollTemplateDTO template = pollTemplateService.getPollTemplate(municipalityUserId, templateId);
        return ResponseEntity.ok(template);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @PatchMapping("/{templateId}/deactivate")
    public ResponseEntity<Void> deactivatePollTemplate(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID templateId
    ) {
        log.info("Request to deactivate poll template ID: {} by municipality user ID: {}", templateId, municipalityUserId);
        pollTemplateService.deactivatePollTemplate(municipalityUserId, templateId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deletePollTemplate(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId,
            @PathVariable UUID templateId
    ) {
        log.info("Request to delete poll template ID: {} by municipality user ID: {}", templateId, municipalityUserId);
        pollTemplateService.deletePollTemplate(municipalityUserId, templateId);
        return ResponseEntity.noContent().build();
    }
}
