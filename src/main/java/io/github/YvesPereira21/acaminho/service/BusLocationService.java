package io.github.YvesPereira21.acaminho.service;

import org.springframework.data.geo.Point;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BusLocationService {

    private final ConcurrentHashMap<String, Point> locationCache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate simpMessagingTemplate;

    public BusLocationService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void updateLocation(UUID busId, float latitude, float longitude) {
        Point newLocation = new Point(latitude, longitude);
        locationCache.put(busId.toString(), newLocation);

        simpMessagingTemplate.convertAndSend("/topic/live-location", newLocation);
    }

    public Point getLatestLocation(UUID busId) {
        return locationCache.get(busId.toString());
    }

    public Map<String, Point> getAllLocations() {
        return locationCache;
    }
}