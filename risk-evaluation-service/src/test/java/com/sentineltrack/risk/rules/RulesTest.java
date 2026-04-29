package com.sentineltrack.risk.rules;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {

    private final UserBehaviorStateService stateService = new UserBehaviorStateService();

    @Test
    void geoMismatchReturns40ForHighRiskCountries() {
        GeoMismatchRule rule = new GeoMismatchRule(stateService);
        for (String country : new String[]{"Russia", "China", "North Korea", "Unknown"}) {
            LoginEvent event = LoginEvent.builder()
                    .eventId("evt-" + country)
                    .userId("user-1")
                    .country(country)
                    .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                    .eventType(EventType.LOGIN_SUCCESS)
                    .success(true)
                    .build();
            assertThat(rule.evaluate(event)).isEqualTo(40);
        }
    }

    @Test
    void newDeviceReturns25ForNewDevice() {
        NewDeviceRule rule = new NewDeviceRule(stateService);
        LoginEvent event = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .deviceId("device-new")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(rule.evaluate(event)).isEqualTo(25);
    }

    @Test
    void suspiciousLoginTimeReturns15For2amUtc() {
        SuspiciousLoginTimeRule rule = new SuspiciousLoginTimeRule();
        LoginEvent event = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .timestamp(Instant.parse("2026-04-28T02:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(rule.evaluate(event)).isEqualTo(15);
    }

    @Test
    void failedLoginRuleIncreasesWithRepeatedFailures() {
        FailedLoginRule rule = new FailedLoginRule(stateService);

        String userId = "user-1";
        for (int i = 0; i < 3; i++) {
            stateService.recordEvent(LoginEvent.builder()
                    .eventId("prev-" + i)
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

        assertThat(rule.evaluate(currentFailure)).isEqualTo(30);
    }

    @Test
    void impossibleTravelReturns50WhenCountryDiffAndGapLessThan60Minutes() {
        ImpossibleTravelRule rule = new ImpossibleTravelRule(stateService);

        String userId = "user-1";
        stateService.recordEvent(LoginEvent.builder()
                .eventId("prev-success")
                .userId(userId)
                .country("United States")
                .city("New York")
                .timestamp(Instant.now().minusSeconds(30 * 60))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build());

        LoginEvent current = LoginEvent.builder()
                .eventId("current-success")
                .userId(userId)
                .country("France")
                .city("Paris")
                .timestamp(Instant.now())
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(rule.evaluate(current)).isEqualTo(50);
    }
}

