package com.sentineltrack.dashboard.controller;

import com.sentineltrack.common.enums.AlertSeverity;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.dashboard.dto.SessionInfo;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardTestController {

    private final DashboardService dashboardService;

    @PostMapping("/mock-alert")
    public ResponseEntity<AlertEvent> createMockAlert() {
        AlertEvent alert = AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .riskEventId(UUID.randomUUID().toString())
                .userId("test-user-" + System.currentTimeMillis())
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .severity(AlertSeverity.HIGH)
                .message("Test alert created via test endpoint")
                .riskScore(85)
                .reasons(List.of("Test reason 1", "Test reason 2"))
                .build();

        dashboardService.addAlert(alert);
        log.info("Created mock alert: {}", alert.getAlertId());
        return ResponseEntity.ok(alert);
    }

    @PostMapping("/mock-risk")
    public ResponseEntity<RiskEvent> createMockRisk() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceEventId(UUID.randomUUID().toString())
                .userId("test-user-" + System.currentTimeMillis())
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .reasons(List.of("Test risk reason"))
                .build();

        dashboardService.addRiskEvent(riskEvent);
        log.info("Created mock risk event: {}", riskEvent.getEventId());
        return ResponseEntity.ok(riskEvent);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAllData() {
        dashboardService.clearAll();
        log.info("Cleared all dashboard data via test endpoint");
        return ResponseEntity.noContent().build();
    }
}
