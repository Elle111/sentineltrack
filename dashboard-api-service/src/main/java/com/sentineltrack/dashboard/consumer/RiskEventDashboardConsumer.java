package com.sentineltrack.dashboard.consumer;

import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventDashboardConsumer {

    private final DashboardService dashboardService;

    @KafkaListener(
            topics = "${sentineltrack.kafka.topics.risk-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {
                "spring.json.value.default.type=com.sentineltrack.common.events.RiskEvent"
            }
    )
    public void consumeRiskEvent(RiskEvent riskEvent) {
        try {
            log.info("Consumed risk event: eventId={}, userId={}, riskLevel={}, riskScore={}",
                    riskEvent.getEventId(),
                    riskEvent.getUserId(),
                    riskEvent.getRiskLevel(),
                    riskEvent.getRiskScore());

            dashboardService.addRiskEvent(riskEvent);
        } catch (Exception e) {
            log.error("Error processing risk event: eventId={}", riskEvent != null ? riskEvent.getEventId() : "null", e);
        }
    }
}
