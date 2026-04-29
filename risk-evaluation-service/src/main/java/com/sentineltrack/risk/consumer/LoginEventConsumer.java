package com.sentineltrack.risk.consumer;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.producer.RiskEventProducer;
import com.sentineltrack.risk.service.RiskEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginEventConsumer {

    private final RiskEvaluationService riskEvaluationService;
    private final RiskEventProducer riskEventProducer;

    @KafkaListener(topics = "${sentineltrack.kafka.topics.login-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLoginEvent(LoginEvent loginEvent) {
        try {
            log.info("Consumed login event: {}", loginEvent);

            var riskEvent = riskEvaluationService.evaluateRisk(loginEvent);
            riskEventProducer.sendRiskEvent(riskEvent);
        } catch (Exception e) {
            log.error("Error processing login event: {}", loginEvent, e);
        }
    }
}
