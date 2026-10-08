package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.TravelControllerOpenApi;
import io.a_caminho.backend.dto.poll.PollPassengerDTO;
import io.a_caminho.backend.dto.travel.TravelCancelDTO;
import io.a_caminho.backend.dto.travel.TravelCreateDTO;
import io.a_caminho.backend.dto.travel.TravelDTO;
import io.a_caminho.backend.service.TravelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelController implements TravelControllerOpenApi {

    private final TravelService travelService;

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @PostMapping
    public ResponseEntity<TravelDTO> createTravel(
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId,
            @Valid @RequestBody TravelCreateDTO travelDTO
    ) {
        log.info("Request to create travel by bus driver user ID: {}", busDriverUserId);
        TravelDTO createdTravel = travelService.createTravel(busDriverUserId, travelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTravel);
    }

    @Override
    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @GetMapping("/{travelId}")
    public ResponseEntity<TravelDTO> getTravelById(
            @PathVariable UUID travelId,
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        log.info("Request to fetch travel ID: {} by user ID: {}", travelId, userId);
        TravelDTO travel = travelService.getTravelById(travelId, userId);
        return ResponseEntity.ok(travel);
    }

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @GetMapping("/active")
    public ResponseEntity<TravelDTO> getActiveTravelByDriver(
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch active travel for bus driver user ID: {}", busDriverUserId);
        TravelDTO activeTravel = travelService.getActiveTravelByDriver(busDriverUserId);
        return ResponseEntity.ok(activeTravel);
    }

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @GetMapping("/driver")
    public ResponseEntity<List<TravelDTO>> getAllTravelsByDriver(
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to fetch all travels for bus driver user ID: {}", busDriverUserId);
        List<TravelDTO> travels = travelService.getAllTravelsByDriver(busDriverUserId);
        return ResponseEntity.ok(travels);
    }

    @Override
    @PreAuthorize("hasRole('MUNICIPALITY')")
    @GetMapping("/municipality")
    public ResponseEntity<List<TravelDTO>> getAllTravelsByMunicipality(
            @AuthenticationPrincipal(expression = "id") UUID municipalityUserId
    ) {
        log.info("Request to fetch all travels for municipality user ID: {}", municipalityUserId);
        List<TravelDTO> travels = travelService.getAllTravelsByMunicipality(municipalityUserId);
        return ResponseEntity.ok(travels);
    }

    @Override
    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @GetMapping("/{travelId}/passengers")
    public ResponseEntity<List<PollPassengerDTO>> getTravelPassengers(
            @PathVariable UUID travelId,
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        log.info("Request to fetch passengers for travel ID: {} by user ID: {}", travelId, userId);
        List<PollPassengerDTO> passengers = travelService.getTravelPassengers(travelId, userId);
        return ResponseEntity.ok(passengers);
    }

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @PatchMapping("/{travelId}/start-return")
    public ResponseEntity<TravelDTO> startReturn(
            @PathVariable UUID travelId,
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to start return for travel ID: {} by driver user ID: {}", travelId, busDriverUserId);
        TravelDTO updatedTravel = travelService.startReturn(travelId, busDriverUserId);
        return ResponseEntity.ok(updatedTravel);
    }

    @Override
    @PreAuthorize("hasRole('BUS_DRIVER')")
    @PatchMapping("/{travelId}/complete")
    public ResponseEntity<TravelDTO> completeTravel(
            @PathVariable UUID travelId,
            @AuthenticationPrincipal(expression = "id") UUID busDriverUserId
    ) {
        log.info("Request to complete travel ID: {} by driver user ID: {}", travelId, busDriverUserId);
        TravelDTO updatedTravel = travelService.completeTravel(travelId, busDriverUserId);
        return ResponseEntity.ok(updatedTravel);
    }

    @Override
    @PreAuthorize("hasAnyRole('BUS_DRIVER', 'MUNICIPALITY')")
    @PatchMapping("/{travelId}/cancel")
    public ResponseEntity<TravelDTO> cancelTravel(
            @PathVariable UUID travelId,
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody TravelCancelDTO cancelDTO
    ) {
        log.info("Request to cancel travel ID: {} by user ID: {}", travelId, userId);
        TravelDTO updatedTravel = travelService.cancelTravel(travelId, userId, cancelDTO.cancellationReason());
        return ResponseEntity.ok(updatedTravel);
    }
}
