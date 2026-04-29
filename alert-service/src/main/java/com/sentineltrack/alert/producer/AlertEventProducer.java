package com.sentineltrack.alert.producer;

import com.sentineltrack.common.events.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEventProducer {

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;

    @Value("${sentineltrack.kafka.topics.alerts}")
    private String topic;

    public void sendAlert(AlertEvent alertEvent) {
        kafkaTemplate.send(topic, alertEvent.getUserId(), alertEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published alert: alertId={}, userId={}, severity={}, riskScore={}",
                                alertEvent.getAlertId(),
                                alertEvent.getUserId(),
                                alertEvent.getSeverity(),
                                alertEvent.getRiskScore());
                    } else {
                        log.error("Failed to publish alert: alertId={}, userId={}",
                                alertEvent.getAlertId(),
                                alertEvent.getUserId(),
                                ex);
                    }
                });
    }
}
