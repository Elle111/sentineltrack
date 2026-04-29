package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class SuspiciousLoginTimeRule implements RiskRule {

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null || event.getTimestamp() == null) return 0;

        log.debug("Evaluating suspicious login time rule for userId={}", event.getUserId());

        LocalDateTime loginTime = LocalDateTime.ofInstant(event.getTimestamp(), ZoneOffset.UTC);
        int hour = loginTime.getHour();
        
        // Flag logins between 11 PM and 5 AM
        if (hour >= 23 || hour < 5) {
            return 15;
        }
        return 0;
    }

    @Override
    public String reason() {
        return "Login at unusual time";
    }
}
