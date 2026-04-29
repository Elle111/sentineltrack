package com.sentineltrack.risk.service;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.rules.*;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskEvaluationServiceTest {

    @Test
    void normalLoginReturnsLowAfterBaselineRecorded() {
        UserBehaviorStateService stateService = new UserBehaviorStateService();
        RiskEvaluationService service = new RiskEvaluationService(
                List.of(
                        new FailedLoginRule(stateService),
                        new GeoMismatchRule(stateService),
                        new ImpossibleTravelRule(stateService),
                        new NewDeviceRule(stateService),
                        new SuspiciousLoginTimeRule(),
                        new AnomalyLoginFrequencyRule(stateService)
                ),
                stateService
        );

        LoginEvent baseline = LoginEvent.builder()
                .eventId("evt-baseline")
                .userId("user-1")
                .deviceId("device-1")
                .country("United States")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();
        service.evaluateRisk(baseline);

        LoginEvent normal = LoginEvent.builder()
                .eventId("evt-normal")
                .userId("user-1")
                .deviceId("device-1")
                .country("United States")
                .timestamp(Instant.parse("2026-04-28T10:05:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(service.evaluateRisk(normal).getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void suspiciousCountryReturnsMedium() {
        UserBehaviorStateService stateService = new UserBehaviorStateService();
        RiskEvaluationService service = new RiskEvaluationService(
                List.of(new GeoMismatchRule(stateService), new SuspiciousLoginTimeRule()),
                stateService
        );

        LoginEvent suspicious = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .country("Russia")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(service.evaluateRisk(suspicious).getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void failedLoginReturnsMediumWithRepeatedFailures() {
        UserBehaviorStateService stateService = new UserBehaviorStateService();
        RiskEvaluationService service = new RiskEvaluationService(
                List.of(new FailedLoginRule(stateService)),
                stateService
        );

        String userId = "user-1";
        for (int i = 0; i < 3; i++) {
            service.evaluateRisk(LoginEvent.builder()
                    .eventId("evt-fail-" + i)
                    .userId(userId)
                    .timestamp(Instant.now().minusSeconds(60L * i))
                    .eventType(EventType.LOGIN_FAILURE)
                    .success(false)
                    .build());
        }

        LoginEvent currentFailure = LoginEvent.builder()
                .eventId("evt-current")
                .userId(userId)
                .timestamp(Instant.now())
                .eventType(EventType.LOGIN_FAILURE)
                .success(false)
                .build();

        assertThat(service.evaluateRisk(currentFailure).getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void riskyCountryAndNewDeviceCanReturnHighOrAbove() {
        UserBehaviorStateService stateService = new UserBehaviorStateService();
        RiskEvaluationService service = new RiskEvaluationService(
                List.of(new GeoMismatchRule(stateService), new NewDeviceRule(stateService), new SuspiciousLoginTimeRule()),
                stateService
        );

        LoginEvent event = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .deviceId("device-new-1")
                .country("Russia")
                .timestamp(Instant.parse("2026-04-28T02:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        RiskLevel level = service.evaluateRisk(event).getRiskLevel();
        assertThat(level).isIn(RiskLevel.HIGH, RiskLevel.CRITICAL);
    }
}
