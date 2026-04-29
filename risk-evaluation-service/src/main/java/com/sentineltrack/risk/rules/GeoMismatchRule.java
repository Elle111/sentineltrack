package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeoMismatchRule implements RiskRule {

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("Russia", "China", "North Korea", "Unknown");
    private final UserBehaviorStateService userBehaviorStateService;

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null) return 0;
        String userId = event.getUserId();
        String country = safeTrim(event.getCountry());

        log.debug("Evaluating geo mismatch rule for userId={}, country={}", userId, country);

        if (country == null) {
            return 10;
        }
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            return 40;
        }
        if (userBehaviorStateService.isNewCountry(event)) {
            return 25;
        }
        return 0;
    }

    @Override
    public String reason() {
        return "Login from unusual geographic location";
    }

    private static String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
