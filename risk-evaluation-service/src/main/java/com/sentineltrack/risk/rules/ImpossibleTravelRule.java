package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImpossibleTravelRule implements RiskRule {

    private final UserBehaviorStateService userBehaviorStateService;

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null) return 0;
        if (!event.isSuccess()) return 0;

        String userId = event.getUserId();
        LoginEvent previous = userBehaviorStateService.getLastSuccessfulLogin(userId);
        if (previous == null) return 0;

        String prevCountry = safeTrim(previous.getCountry());
        String currentCountry = safeTrim(event.getCountry());
        if (prevCountry == null || currentCountry == null) return 0;
        if (prevCountry.equalsIgnoreCase(currentCountry)) return 0;

        Instant prevTs = previous.getTimestamp();
        Instant currentTs = event.getTimestamp();
        if (prevTs == null || currentTs == null) return 0;

        long minutes = Math.abs(Duration.between(prevTs, currentTs).toMinutes());
        log.debug("Evaluating impossible travel rule userId={}, prevCountry={}, currentCountry={}, minutes={}",
                userId, prevCountry, currentCountry, minutes);

        if (minutes < 60) return 50;
        if (minutes < 180) return 35;
        return 0;
    }

    @Override
    public String reason() {
        return "Impossible travel detected";
    }

    private static String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
