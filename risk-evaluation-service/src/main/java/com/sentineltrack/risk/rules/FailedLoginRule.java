package com.sentineltrack.risk.rules;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailedLoginRule implements RiskRule {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final UserBehaviorStateService userBehaviorStateService;

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null) return 0;
        boolean failed = event.getEventType() == EventType.LOGIN_FAILURE || !event.isSuccess();
        if (!failed) return 0;

        String userId = event.getUserId();
        long previousFailedAttempts = userBehaviorStateService.countRecentFailedLogins(userId, WINDOW);
        log.debug("Evaluating failed login rule for userId={}, previousFailedAttempts={}", userId, previousFailedAttempts);

        if (previousFailedAttempts >= 5) return 45;
        if (previousFailedAttempts >= 3) return 30;
        return 20;
    }

    @Override
    public String reason() {
        return "Multiple failed login attempts detected";
    }
}
