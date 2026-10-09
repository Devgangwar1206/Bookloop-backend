package com.bookloop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final Instant startTime = Instant.now();

    /**
     * Dedicated health check endpoint for uptime monitoring and preventing Render cold start / sleep.
     * Can be called by frontend automated keep-alive ping or external ping services (like Cron-job / UptimeRobot).
     */
    @GetMapping({"/health", "/api/health", "/api/v1/health"})
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "bookloop-backend");
        response.put("message", "BookLoop backend is active and healthy");
        response.put("startTime", startTime.toString());
        response.put("timestamp", Instant.now().toEpochMilli());
        return ResponseEntity.ok(response);
    }
}
