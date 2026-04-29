package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnomalyLoginFrequencyRule implements RiskRule {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final UserBehaviorStateService userBehaviorStateService;

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null) return 0;
        if (event.getUserId() == null || event.getUserId().trim().isEmpty()) return 0;

        long failedCount = userBehaviorStateService.countRecentFailedLogins(event.getUserId(), WINDOW);
        log.debug("Evaluating anomaly login frequency rule userId={}, failedCount={}", event.getUserId(), failedCount);

        if (failedCount > 10) return 50;
        if (failedCount > 5) return 35;
        return 0;
    }

    @Override
    public String reason() {
        return "Unusual login failure frequency detected";
    }
}

