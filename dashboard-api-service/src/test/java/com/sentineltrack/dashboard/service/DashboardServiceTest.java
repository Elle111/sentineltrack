package com.sentineltrack.dashboard.service;

import com.sentineltrack.common.enums.AlertSeverity;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.dashboard.dto.DashboardSummary;
import com.sentineltrack.dashboard.dto.SessionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService();
    }

    @Test
    void addAlert_storesAlert() {
        AlertEvent alert = AlertEvent.builder()
                .alertId("alert-001")
                .riskEventId("risk-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .severity(AlertSeverity.HIGH)
                .message("Test alert")
                .riskScore(75)
                .reasons(List.of("Test reason"))
                .build();

        dashboardService.addAlert(alert);

        List<AlertEvent> alerts = dashboardService.getRecentAlerts();
        assertEquals(1, alerts.size());
        assertEquals("alert-001", alerts.get(0).getAlertId());
    }

    @Test
    void addRiskEvent_storesRiskEvent() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-001")
                .sourceEventId("evt-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .reasons(List.of("Test reason"))
                .build();

        dashboardService.addRiskEvent(riskEvent);

        List<RiskEvent> riskEvents = dashboardService.getRecentRiskEvents();
        assertEquals(1, riskEvents.size());
        assertEquals("risk-001", riskEvents.get(0).getEventId());
    }

    @Test
    void getRecentAlerts_sortedNewestFirst() {
        Instant now = Instant.now();
        AlertEvent alert1 = AlertEvent.builder()
                .alertId("alert-001")
                .timestamp(now.minusSeconds(100))
                .build();
        AlertEvent alert2 = AlertEvent.builder()
                .alertId("alert-002")
                .timestamp(now.minusSeconds(50))
                .build();
        AlertEvent alert3 = AlertEvent.builder()
                .alertId("alert-003")
                .timestamp(now)
                .build();

        dashboardService.addAlert(alert1);
        dashboardService.addAlert(alert2);
        dashboardService.addAlert(alert3);

        List<AlertEvent> alerts = dashboardService.getRecentAlerts();
        assertEquals(3, alerts.size());
        assertEquals("alert-003", alerts.get(0).getAlertId());
        assertEquals("alert-002", alerts.get(1).getAlertId());
        assertEquals("alert-001", alerts.get(2).getAlertId());
    }

    @Test
    void getRecentRiskEvents_sortedNewestFirst() {
        Instant now = Instant.now();
        RiskEvent risk1 = RiskEvent.builder()
                .eventId("risk-001")
                .sessionId("session-001")
                .userId("user-123")
                .timestamp(now.minusSeconds(100))
                .riskScore(50)
                .riskLevel(RiskLevel.LOW)
                .build();
        RiskEvent risk2 = RiskEvent.builder()
                .eventId("risk-002")
                .sessionId("session-002")
                .userId("user-123")
                .timestamp(now.minusSeconds(50))
                .riskScore(60)
                .riskLevel(RiskLevel.MEDIUM)
                .build();
        RiskEvent risk3 = RiskEvent.builder()
                .eventId("risk-003")
                .sessionId("session-003")
                .userId("user-123")
                .timestamp(now)
                .riskScore(70)
                .riskLevel(RiskLevel.HIGH)
                .build();

        dashboardService.addRiskEvent(risk1);
        dashboardService.addRiskEvent(risk2);
        dashboardService.addRiskEvent(risk3);

        List<RiskEvent> riskEvents = dashboardService.getRecentRiskEvents();
        assertEquals(3, riskEvents.size());
        assertEquals("risk-003", riskEvents.get(0).getEventId());
        assertEquals("risk-002", riskEvents.get(1).getEventId());
        assertEquals("risk-001", riskEvents.get(2).getEventId());
    }

    @Test
    void updateSessionFromRiskEvent_createsNewSession() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .build();

        dashboardService.updateSessionFromRiskEvent(riskEvent);

        List<SessionInfo> sessions = dashboardService.getRecentSessions();
        assertEquals(1, sessions.size());
        assertEquals("session-001", sessions.get(0).getSessionId());
        assertEquals("user-123", sessions.get(0).getUserId());
        assertEquals(75, sessions.get(0).getRiskScore());
        assertEquals(RiskLevel.HIGH, sessions.get(0).getRiskLevel());
    }

    @Test
    void updateSessionFromRiskEvent_updatesExistingSession() {
        RiskEvent riskEvent1 = RiskEvent.builder()
                .eventId("risk-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now().minusSeconds(100))
                .riskScore(50)
                .riskLevel(RiskLevel.MEDIUM)
                .build();

        RiskEvent riskEvent2 = RiskEvent.builder()
                .eventId("risk-002")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .riskScore(85)
                .riskLevel(RiskLevel.HIGH)
                .build();

        dashboardService.updateSessionFromRiskEvent(riskEvent1);
        dashboardService.updateSessionFromRiskEvent(riskEvent2);

        List<SessionInfo> sessions = dashboardService.getRecentSessions();
        assertEquals(1, sessions.size());
        assertEquals(85, sessions.get(0).getRiskScore());
        assertEquals(RiskLevel.HIGH, sessions.get(0).getRiskLevel());
    }

    @Test
    void getSummary_returnsCorrectCounts() {
        AlertEvent alert1 = AlertEvent.builder()
                .alertId("alert-001")
                .severity(AlertSeverity.HIGH)
                .build();
        AlertEvent alert2 = AlertEvent.builder()
                .alertId("alert-002")
                .severity(AlertSeverity.CRITICAL)
                .build();

        RiskEvent risk1 = RiskEvent.builder()
                .eventId("risk-001")
                .sessionId("session-001")
                .userId("user-123")
                .timestamp(Instant.now())
                .riskScore(50)
                .riskLevel(RiskLevel.MEDIUM)
                .build();
        RiskEvent risk2 = RiskEvent.builder()
                .eventId("risk-002")
                .sessionId("session-002")
                .userId("user-123")
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .build();
        RiskEvent risk3 = RiskEvent.builder()
                .eventId("risk-003")
                .sessionId("session-003")
                .userId("user-123")
                .timestamp(Instant.now())
                .riskScore(95)
                .riskLevel(RiskLevel.CRITICAL)
                .build();

        SessionInfo session1 = SessionInfo.builder()
                .sessionId("session-001")
                .riskLevel(RiskLevel.HIGH)
                .build();
        SessionInfo session2 = SessionInfo.builder()
                .sessionId("session-002")
                .riskLevel(RiskLevel.LOW)
                .build();

        dashboardService.addAlert(alert1);
        dashboardService.addAlert(alert2);
        dashboardService.addRiskEvent(risk1);
        dashboardService.addRiskEvent(risk2);
        dashboardService.addRiskEvent(risk3);
        dashboardService.addSession(session1);
        dashboardService.addSession(session2);

        DashboardSummary summary = dashboardService.getSummary();

        assertEquals(2, summary.getTotalAlerts());
        assertEquals(1, summary.getCriticalAlerts());
        assertEquals(1, summary.getMediumRiskEvents());
        assertEquals(1, summary.getHighRiskEvents());
        assertEquals(1, summary.getCriticalRiskEvents());
        assertEquals(3, summary.getTotalSessions());
        assertEquals(1, summary.getHighRiskSessions());
    }

    @Test
    void clearAll_clearsAllMaps() {
        AlertEvent alert = AlertEvent.builder()
                .alertId("alert-001")
                .build();
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-001")
                .sessionId("session-001")
                .userId("user-123")
                .timestamp(Instant.now())
                .riskScore(50)
                .riskLevel(RiskLevel.LOW)
                .build();
        SessionInfo session = SessionInfo.builder()
                .sessionId("session-001")
                .build();

        dashboardService.addAlert(alert);
        dashboardService.addRiskEvent(riskEvent);
        dashboardService.addSession(session);

        dashboardService.clearAll();

        assertTrue(dashboardService.getRecentAlerts().isEmpty());
        assertTrue(dashboardService.getRecentRiskEvents().isEmpty());
        assertTrue(dashboardService.getRecentSessions().isEmpty());
    }

    @Test
    void addRiskEvent_updatesSessionFromRiskEvent() {
        RiskEvent riskEvent = RiskEvent.builder()
                .eventId("risk-001")
                .userId("user-123")
                .sessionId("session-001")
                .timestamp(Instant.now())
                .riskScore(75)
                .riskLevel(RiskLevel.HIGH)
                .build();

        dashboardService.addRiskEvent(riskEvent);

        List<SessionInfo> sessions = dashboardService.getRecentSessions();
        assertEquals(1, sessions.size());
        assertEquals("session-001", sessions.get(0).getSessionId());
    }
}
