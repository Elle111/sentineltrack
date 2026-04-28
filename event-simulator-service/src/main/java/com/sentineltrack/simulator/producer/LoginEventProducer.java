package com.sentineltrack.simulator.producer;

import com.sentineltrack.common.events.LoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginEventProducer {

    private final KafkaTemplate<String, LoginEvent> kafkaTemplate;

    @Value("${sentineltrack.kafka.topics.login-events}")
    private String topic;

    public void sendLoginEvent(LoginEvent event) {
        CompletableFuture<SendResult<String, LoginEvent>> future = kafkaTemplate.send(topic, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published login event: eventId={}, userId={}, type={}",
                        event.getEventId(), event.getUserId(), event.getEventType());
            } else {
                log.error("Failed to publish login event: eventId={}, userId={}, error={}",
                        event.getEventId(), event.getUserId(), ex.getMessage(), ex);
            }
        });
    }
}
