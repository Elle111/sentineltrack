package com.sentineltrack.alert.service;

import com.sentineltrack.common.enums.AlertSeverity;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AlertService {

    public Optional<AlertEvent> createAlert(RiskEvent riskEvent) {
        if (riskEvent == null) {
            log.warn("Received null RiskEvent, skipping alert creation");
            return Optional.empty();
        }

        if (riskEvent.getRiskLevel() == RiskLevel.LOW) {
            log.info("LOW risk event ignored for user: {}", riskEvent.getUserId());
            return Optional.empty();
        }

        if (riskEvent.getRiskLevel() == RiskLevel.MEDIUM) {
            log.info("MEDIUM risk event ignored for user: {}", riskEvent.getUserId());
            return Optional.empty();
        }

        AlertSeverity severity = riskEvent.getRiskLevel() == RiskLevel.CRITICAL
                ? AlertSeverity.CRITICAL
                : AlertSeverity.HIGH;

        String message;
        if (severity == AlertSeverity.CRITICAL) {
            message = String.format("Critical identity threat detected for user %s", riskEvent.getUserId());
        } else {
            message = String.format("High risk session detected for user %s", riskEvent.getUserId());
        }

        AlertEvent alertEvent = AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .riskEventId(riskEvent.getEventId())
                .userId(riskEvent.getUserId())
                .sessionId(riskEvent.getSessionId())
                .timestamp(Instant.now())
                .severity(severity)
                .message(message)
                .riskScore(riskEvent.getRiskScore())
                .reasons(riskEvent.getReasons())
                .build();

        log.info("Created {} alert for user: {}, alertId: {}", severity, riskEvent.getUserId(), alertEvent.getAlertId());
        return Optional.of(alertEvent);
    }
}
