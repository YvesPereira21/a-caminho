package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.bus.BusLocationDTO;
import io.a_caminho.backend.model.Bus;
import io.a_caminho.backend.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLocationService {

    private final ConcurrentHashMap<UUID, BusLocationDTO> locationCache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BusRepository busRepository;

    public void updateLocation(UUID busId, float latitude, float longitude) {
        BusLocationDTO newLocation = new BusLocationDTO(busId, latitude, longitude, LocalDateTime.now());
        locationCache.put(busId, newLocation);

        log.debug("Broadcasting location update for bus {}: lat={}, lng={}", busId, latitude, longitude);

        // 1. Tópico específico por ônibus (para o estudante escutar apenas a sua rota)
        simpMessagingTemplate.convertAndSend("/topic/buses/" + busId + "/location", newLocation);

        // 2. Tópico geral (para monitoramento geral de frota)
        simpMessagingTemplate.convertAndSend("/topic/live-location", newLocation);
    }

    public BusLocationDTO getLatestLocation(UUID busId) {
        return locationCache.get(busId);
    }

    public Map<UUID, BusLocationDTO> getAllLocations() {
        return Map.copyOf(locationCache);
    }

    public Map<UUID, BusLocationDTO> getLocationsForMunicipalityUser(UUID municipalityUserId) {
        List<Bus> buses = busRepository.findAllByMunicipality_User_UserId(municipalityUserId);
        Set<UUID> municipalityBusIds = buses.stream()
                .map(Bus::getBusId)
                .collect(Collectors.toSet());

        return locationCache.entrySet().stream()
                .filter(entry -> municipalityBusIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

