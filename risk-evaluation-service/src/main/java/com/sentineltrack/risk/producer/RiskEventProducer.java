package com.sentineltrack.risk.producer;

import com.sentineltrack.common.events.RiskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEventProducer {

    private final KafkaTemplate<String, RiskEvent> kafkaTemplate;

    @Value("${sentineltrack.kafka.topics.risk-events}")
    private String topic;

    public void sendRiskEvent(RiskEvent event) {
        if (event == null) {
            log.warn("Skipping send: RiskEvent is null");
            return;
        }

        String key = event.getUserId();
        log.info("Sending risk event to topic {}: eventId={}, userId={}, riskScore={}, riskLevel={}",
                topic, event.getEventId(), event.getUserId(), event.getRiskScore(), event.getRiskLevel());
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        var meta = result != null && result.getRecordMetadata() != null ? result.getRecordMetadata() : null;
                        if (meta != null) {
                            log.info("Sent risk event eventId={} userId={} topic={} partition={} offset={}",
                                    event.getEventId(), event.getUserId(), meta.topic(), meta.partition(), meta.offset());
                        } else {
                            log.info("Sent risk event eventId={} userId={} topic={}", event.getEventId(), event.getUserId(), topic);
                        }
                    } else {
                        log.error("Failed to send risk event eventId={} userId={} topic={}",
                                event.getEventId(), event.getUserId(), topic, ex);
                    }
                });
    }
}
