package com.sentineltrack.risk.state;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.events.LoginEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserBehaviorStateServiceTest {

    private final UserBehaviorStateService stateService = new UserBehaviorStateService();

    @Test
    void firstDeviceIsNew() {
        LoginEvent event = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .deviceId("device-1")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(stateService.isNewDevice(event)).isTrue();
    }

    @Test
    void sameDeviceIsNotNewAfterRecordEvent() {
        LoginEvent first = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .deviceId("device-1")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();
        stateService.recordEvent(first);

        LoginEvent second = LoginEvent.builder()
                .eventId("evt-2")
                .userId("user-1")
                .deviceId("device-1")
                .timestamp(Instant.parse("2026-04-28T10:05:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(stateService.isNewDevice(second)).isFalse();
    }

    @Test
    void newCountryDetectionWorks() {
        LoginEvent first = LoginEvent.builder()
                .eventId("evt-1")
                .userId("user-1")
                .country("United States")
                .timestamp(Instant.parse("2026-04-28T10:00:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();
        stateService.recordEvent(first);

        LoginEvent second = LoginEvent.builder()
                .eventId("evt-2")
                .userId("user-1")
                .country("France")
                .timestamp(Instant.parse("2026-04-28T10:05:00Z"))
                .eventType(EventType.LOGIN_SUCCESS)
                .success(true)
                .build();

        assertThat(stateService.isNewCountry(second)).isTrue();
    }

    @Test
    void failedLoginCountWithin15MinutesWorks() {
        Instant now = Instant.now();
        for (int i = 0; i < 4; i++) {
            stateService.recordEvent(LoginEvent.builder()
                    .eventId("evt-f-" + i)
                    .userId("user-1")
                    .timestamp(now.minusSeconds(60L * i))
                    .eventType(EventType.LOGIN_FAILURE)
                    .success(false)
                    .build());
        }
        stateService.recordEvent(LoginEvent.builder()
                .eventId("evt-old")
                .userId("user-1")
                .timestamp(now.minus(Duration.ofMinutes(30)))
                .eventType(EventType.LOGIN_FAILURE)
                .success(false)
                .build());

        assertThat(stateService.countRecentFailedLogins("user-1", Duration.ofMinutes(15))).isEqualTo(4);
    }
}

