package com.sentineltrack.alert.consumer;

import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.alert.producer.AlertEventProducer;
import com.sentineltrack.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventConsumer {

    private final AlertService alertService;
    private final AlertEventProducer alertEventProducer;

    @KafkaListener(
            topics = "${sentineltrack.kafka.topics.risk-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeRiskEvent(RiskEvent riskEvent) {
        try {
            log.info("Consumed risk event: eventId={}, userId={}, riskLevel={}, riskScore={}",
                    riskEvent.getEventId(),
                    riskEvent.getUserId(),
                    riskEvent.getRiskLevel(),
                    riskEvent.getRiskScore());

            alertService.createAlert(riskEvent).ifPresent(alertEventProducer::sendAlert);
        } catch (Exception e) {
            log.error("Error processing risk event: eventId={}", riskEvent != null ? riskEvent.getEventId() : "null", e);
        }
    }
}
