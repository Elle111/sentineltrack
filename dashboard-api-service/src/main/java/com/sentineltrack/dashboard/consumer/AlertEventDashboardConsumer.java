package com.sentineltrack.dashboard.consumer;

import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventDashboardConsumer {

    private final DashboardService dashboardService;

    @KafkaListener(
            topics = "${sentineltrack.kafka.topics.alerts}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {
                "spring.json.value.default.type=com.sentineltrack.common.events.AlertEvent"
            }
    )
    public void consumeAlertEvent(AlertEvent alertEvent) {
        try {
            log.info("Consumed alert event: alertId={}, userId={}, severity={}, riskScore={}",
                    alertEvent.getAlertId(),
                    alertEvent.getUserId(),
                    alertEvent.getSeverity(),
                    alertEvent.getRiskScore());

            dashboardService.addAlert(alertEvent);
        } catch (Exception e) {
            log.error("Error processing alert event: alertId={}", alertEvent != null ? alertEvent.getAlertId() : "null", e);
        }
    }
}
