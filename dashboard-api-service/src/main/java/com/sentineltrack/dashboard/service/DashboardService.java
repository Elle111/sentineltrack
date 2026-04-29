package com.sentineltrack.dashboard.service;

import com.sentineltrack.common.enums.AlertSeverity;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.dashboard.dto.DashboardSummary;
import com.sentineltrack.dashboard.dto.SessionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    private static final int MAX_RECENT_RECORDS = 100;

    private final ConcurrentHashMap<String, AlertEvent> alerts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RiskEvent> riskEvents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public List<AlertEvent> getRecentAlerts() {
        return alerts.values().stream()
                .sorted(Comparator.comparing(AlertEvent::getTimestamp).reversed())
                .limit(MAX_RECENT_RECORDS)
                .collect(Collectors.toList());
    }

    public List<RiskEvent> getRecentRiskEvents() {
        return riskEvents.values().stream()
                .sorted(Comparator.comparing(RiskEvent::getTimestamp).reversed())
                .limit(MAX_RECENT_RECORDS)
                .collect(Collectors.toList());
    }

    public List<SessionInfo> getRecentSessions() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(SessionInfo::getStartTime).reversed())
                .limit(MAX_RECENT_RECORDS)
                .collect(Collectors.toList());
    }

    public DashboardSummary getSummary() {
        int totalSessions = sessions.size();
        int totalAlerts = alerts.size();
        int highRiskSessions = (int) sessions.values().stream()
                .filter(s -> s.getRiskLevel() == RiskLevel.HIGH)
                .count();
        int criticalAlerts = (int) alerts.values().stream()
                .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL)
                .count();
        int mediumRiskEvents = (int) riskEvents.values().stream()
                .filter(r -> r.getRiskLevel() == RiskLevel.MEDIUM)
                .count();
        int highRiskEvents = (int) riskEvents.values().stream()
                .filter(r -> r.getRiskLevel() == RiskLevel.HIGH)
                .count();
        int criticalRiskEvents = (int) riskEvents.values().stream()
                .filter(r -> r.getRiskLevel() == RiskLevel.CRITICAL)
                .count();

        return DashboardSummary.builder()
                .totalSessions(totalSessions)
                .totalAlerts(totalAlerts)
                .highRiskSessions(highRiskSessions)
                .criticalAlerts(criticalAlerts)
                .mediumRiskEvents(mediumRiskEvents)
                .highRiskEvents(highRiskEvents)
                .criticalRiskEvents(criticalRiskEvents)
                .lastUpdated(Instant.now())
                .build();
    }

    public void addAlert(AlertEvent alert) {
        alerts.put(alert.getAlertId(), alert);
        if (alerts.size() > MAX_RECENT_RECORDS) {
            removeOldestAlert();
        }
    }

    public void addRiskEvent(RiskEvent riskEvent) {
        riskEvents.put(riskEvent.getEventId(), riskEvent);
        if (riskEvents.size() > MAX_RECENT_RECORDS) {
            removeOldestRiskEvent();
        }
        updateSessionFromRiskEvent(riskEvent);
    }

    public void addSession(SessionInfo session) {
        sessions.put(session.getSessionId(), session);
        if (sessions.size() > MAX_RECENT_RECORDS) {
            removeOldestSession();
        }
    }

    public void updateSessionFromRiskEvent(RiskEvent riskEvent) {
        SessionInfo session = sessions.computeIfAbsent(riskEvent.getSessionId(), sessionId ->
                SessionInfo.builder()
                        .sessionId(sessionId)
                        .userId(riskEvent.getUserId())
                        .startTime(riskEvent.getTimestamp())
                        .lastUpdated(riskEvent.getTimestamp())
                        .active(true)
                        .riskScore(riskEvent.getRiskScore())
                        .riskLevel(riskEvent.getRiskLevel())
                        .build()
        );

        session.setLastUpdated(riskEvent.getTimestamp());
        session.setRiskScore(riskEvent.getRiskScore());
        session.setRiskLevel(riskEvent.getRiskLevel());
    }

    public void clearAll() {
        alerts.clear();
        riskEvents.clear();
        sessions.clear();
        log.info("Cleared all dashboard data");
    }

    public void initializeMockData() {
        for (int i = 0; i < 5; i++) {
            AlertEvent alert = AlertEvent.builder()
                    .alertId(UUID.randomUUID().toString())
                    .riskEventId(UUID.randomUUID().toString())
                    .userId("user-" + i)
                    .sessionId(UUID.randomUUID().toString())
                    .timestamp(Instant.now().minusSeconds(i * 300))
                    .severity(i % 2 == 0 ? AlertSeverity.HIGH : AlertSeverity.CRITICAL)
                    .message("Suspicious activity detected")
                    .riskScore(75 + i * 5)
                    .reasons(List.of("New device", "Unusual location"))
                    .build();
            addAlert(alert);
        }

        for (int i = 0; i < 10; i++) {
            RiskEvent riskEvent = RiskEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .sourceEventId(UUID.randomUUID().toString())
                    .userId("user-" + (i % 5))
                    .sessionId(UUID.randomUUID().toString())
                    .timestamp(Instant.now().minusSeconds(i * 600))
                    .riskScore(30 + i * 7)
                    .riskLevel(i < 3 ? RiskLevel.LOW :
                              i < 6 ? RiskLevel.MEDIUM :
                              i < 8 ? RiskLevel.HIGH : RiskLevel.CRITICAL)
                    .reasons(List.of("Geographic anomaly"))
                    .build();
            addRiskEvent(riskEvent);
        }

        for (int i = 0; i < 15; i++) {
            SessionInfo session = SessionInfo.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .userId("user-" + (i % 5))
                    .ipAddress("192.168.1." + (100 + i))
                    .country("United States")
                    .city("New York")
                    .startTime(Instant.now().minusSeconds(i * 900))
                    .lastUpdated(Instant.now().minusSeconds(i * 900))
                    .active(i % 3 != 0)
                    .riskScore(i * 5)
                    .riskLevel(i < 5 ? RiskLevel.LOW : i < 10 ? RiskLevel.MEDIUM : RiskLevel.HIGH)
                    .build();
            addSession(session);
        }

        log.info("Initialized mock data: {} alerts, {} risk events, {} sessions",
                alerts.size(), riskEvents.size(), sessions.size());
    }

    private void removeOldestAlert() {
        alerts.values().stream()
                .min(Comparator.comparing(AlertEvent::getTimestamp))
                .ifPresent(oldest -> alerts.remove(oldest.getAlertId()));
    }

    private void removeOldestRiskEvent() {
        riskEvents.values().stream()
                .min(Comparator.comparing(RiskEvent::getTimestamp))
                .ifPresent(oldest -> riskEvents.remove(oldest.getEventId()));
    }

    private void removeOldestSession() {
        sessions.values().stream()
                .min(Comparator.comparing(SessionInfo::getStartTime))
                .ifPresent(oldest -> sessions.remove(oldest.getSessionId()));
    }
}
