package com.sentineltrack.simulator.service;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.simulator.producer.LoginEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimulationService {

    private final LoginEventProducer loginEventProducer;
    private final Random random = new Random();

    public void simulateNormalLogin() {
        LoginEvent event = buildLoginEvent(
                EventType.LOGIN_SUCCESS,
                "United States",
                randomFromList(List.of("New York", "Tampa", "Chicago", "Dallas")),
                randomPrivateIp(),
                true
        );
        loginEventProducer.sendLoginEvent(event);
        log.info("Simulated normal login event for user: {}", event.getUserId());
    }

    public void simulateSuspiciousLogin() {
        LoginEvent event = buildLoginEvent(
                EventType.LOGIN_SUCCESS,
                randomFromList(List.of("Russia", "China", "North Korea", "Unknown")),
                randomFromList(List.of("Moscow", "Beijing", "Pyongyang", "Unknown")),
                randomSuspiciousIp(),
                true
        );
        loginEventProducer.sendLoginEvent(event);
        log.info("Simulated suspicious login event for user: {}", event.getUserId());
    }

    public void simulateFailedLogins() {
        String userId = randomUserId();
        String ipAddress = randomPrivateIp();
        String deviceId = randomDeviceId();

        for (int i = 0; i < 5; i++) {
            LoginEvent event = buildLoginEvent(
                    EventType.LOGIN_FAILURE,
                    "United States",
                    "Los Angeles",
                    ipAddress,
                    false,
                    userId,
                    deviceId
            );
            loginEventProducer.sendLoginEvent(event);
        }

        log.info("Simulated 5 failed login events for user: {}", userId);
    }

    public void simulateBulkNormal(int count) {
        count = Math.max(1, Math.min(count, 100));
        log.info("Generating {} normal login events", count);

        for (int i = 0; i < count; i++) {
            LoginEvent event = buildLoginEvent(
                    EventType.LOGIN_SUCCESS,
                    "United States",
                    randomFromList(List.of("New York", "Tampa", "Chicago", "Dallas")),
                    randomPrivateIp(),
                    true
            );
            loginEventProducer.sendLoginEvent(event);
        }

        log.info("Simulated {} normal login events", count);
    }

    public void simulateBulkSuspicious(int count) {
        count = Math.max(1, Math.min(count, 100));
        log.info("Generating {} suspicious login events", count);

        for (int i = 0; i < count; i++) {
            LoginEvent event = buildLoginEvent(
                    EventType.LOGIN_SUCCESS,
                    randomFromList(List.of("Russia", "China", "North Korea", "Unknown")),
                    randomFromList(List.of("Moscow", "Beijing", "Pyongyang", "Unknown")),
                    randomSuspiciousIp(),
                    true
            );
            loginEventProducer.sendLoginEvent(event);
        }

        log.info("Simulated {} suspicious login events", count);
    }

    private LoginEvent buildLoginEvent(EventType eventType, String country, String city, String ipAddress, boolean success) {
        return buildLoginEvent(eventType, country, city, ipAddress, success, randomUserId(), randomDeviceId());
    }

    private LoginEvent buildLoginEvent(EventType eventType, String country, String city, String ipAddress, boolean success,
                                       String userId, String deviceId) {
        return LoginEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .sessionId(randomSessionId())
                .timestamp(Instant.now())
                .eventType(eventType)
                .ipAddress(ipAddress)
                .country(country)
                .city(city)
                .deviceId(deviceId)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .success(success)
                .build();
    }

    private String randomUserId() {
        return "user-" + random.nextInt(1000);
    }

    private String randomSessionId() {
        return UUID.randomUUID().toString();
    }

    private String randomDeviceId() {
        return "device-" + UUID.randomUUID().toString();
    }

    private String randomPrivateIp() {
        return "192.168.1." + random.nextInt(256);
    }

    private String randomSuspiciousIp() {
        return "203.0.113." + random.nextInt(256);
    }

    private <T> T randomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
