package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;

public interface RiskRule {
    int evaluate(LoginEvent event);
    String reason();
}
