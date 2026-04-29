package com.sentineltrack.alert.service;

import com.sentineltrack.common.enums.AlertSeverity;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService();
    }

    @Test
    void createAlert_withNullRiskEvent_returnsEmpty() {
        Optional<AlertEvent> result = alertService.createAlert(null);
        assertFalse(result.isPresent());
    }

    @Test
    void createAlert_withLowRiskLevel_returnsEmpty() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .riskScore(10)
                .riskLevel(RiskLevel.LOW)
                .reasons(List.of())
                .build();

        Optional<AlertEvent> result = alertService.createAlert(riskEvent);
        assertFalse(result.isPresent());
    }

    @Test
    void createAlert_withMediumRiskLevel_returnsEmpty() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-002")
                .userId("user-456")
                .sessionId("session-002")
                .timestamp(Instant.now())
                .riskScore(50)
                .riskLevel(RiskLevel.MEDIUM)
                .reasons(List.of("Some reason"))
                .build();

        Optional<AlertEvent> result = alertService.createAlert(riskEvent);
        assertFalse(result.isPresent());
    }

    @Test
    void createAlert_withHighRiskLevel_createsAlertWithHighSeverity() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-003")
                .userId("user-789")
                .sessionId("session-003")
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .reasons(List.of("Login from unusual location", "New device"))
                .build();

        Optional<AlertEvent> result = alertService.createAlert(riskEvent);

        assertTrue(result.isPresent());
        AlertEvent alertEvent = result.get();
        assertEquals(AlertSeverity.HIGH, alertEvent.getSeverity());
        assertEquals("High risk session detected for user user-789", alertEvent.getMessage());
        assertEquals(riskEvent.getUserId(), alertEvent.getUserId());
        assertEquals(riskEvent.getSessionId(), alertEvent.getSessionId());
        assertEquals(riskEvent.getRiskScore(), alertEvent.getRiskScore());
        assertEquals(riskEvent.getReasons(), alertEvent.getReasons());
        assertEquals(riskEvent.getEventId(), alertEvent.getRiskEventId());
        assertNotNull(alertEvent.getAlertId());
        assertNotNull(alertEvent.getTimestamp());
    }

    @Test
    void createAlert_withCriticalRiskLevel_createsAlertWithCriticalSeverity() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-004")
                .userId("user-999")
                .sessionId("session-004")
                .timestamp(Instant.now())
                .riskScore(95)
                .riskLevel(RiskLevel.CRITICAL)
                .reasons(List.of("Impossible travel", "Multiple failed logins"))
                .build();

        Optional<AlertEvent> result = alertService.createAlert(riskEvent);

        assertTrue(result.isPresent());
        AlertEvent alertEvent = result.get();
        assertEquals(AlertSeverity.CRITICAL, alertEvent.getSeverity());
        assertEquals("Critical identity threat detected for user user-999", alertEvent.getMessage());
        assertEquals(riskEvent.getUserId(), alertEvent.getUserId());
        assertEquals(riskEvent.getSessionId(), alertEvent.getSessionId());
        assertEquals(riskEvent.getRiskScore(), alertEvent.getRiskScore());
        assertEquals(riskEvent.getReasons(), alertEvent.getReasons());
        assertEquals(riskEvent.getEventId(), alertEvent.getRiskEventId());
        assertNotNull(alertEvent.getAlertId());
        assertNotNull(alertEvent.getTimestamp());
    }

    @Test
    void createAlert_copiesFieldsFromRiskEvent() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-005")
                .userId("user-111")
                .sessionId("session-005")
                .timestamp(Instant.now())
                .riskScore(85)
                .riskLevel(RiskLevel.HIGH)
                .reasons(List.of("Reason 1", "Reason 2", "Reason 3"))
                .build();

        Optional<AlertEvent> result = alertService.createAlert(riskEvent);

        assertTrue(result.isPresent());
        AlertEvent alertEvent = result.get();
        assertEquals(riskEvent.getUserId(), alertEvent.getUserId());
        assertEquals(riskEvent.getSessionId(), alertEvent.getSessionId());
        assertEquals(riskEvent.getRiskScore(), alertEvent.getRiskScore());
        assertEquals(riskEvent.getReasons(), alertEvent.getReasons());
    }
}
