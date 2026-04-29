package com.sentineltrack.risk.service;

import com.sentineltrack.common.enums.RiskLevel;
import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.risk.rules.RiskRule;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEvaluationService {

    private final List<RiskRule> rules;
    private final UserBehaviorStateService userBehaviorStateService;

    public RiskEvent evaluateRisk(LoginEvent loginEvent) {
        String eventId = loginEvent != null ? loginEvent.getEventId() : null;
        String userId = loginEvent != null ? loginEvent.getUserId() : null;
        log.info("Evaluating risk for login eventId={}, userId={}", eventId, userId);

        int totalScore = 0;
        Set<String> reasons = new LinkedHashSet<>();

        for (RiskRule rule : rules) {
            int score = rule.evaluate(loginEvent);
            if (score > 0) {
                totalScore += score;
                reasons.add(rule.reason());
                log.debug("Rule '{}' added {} points", rule.reason(), score);
            }
        }

        // Cap score at 100
        totalScore = Math.min(totalScore, 100);

        RiskLevel riskLevel = determineRiskLevel(totalScore);

        RiskEvent riskEvent = RiskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceEventId(eventId)
                .userId(userId)
                .sessionId(loginEvent != null ? loginEvent.getSessionId() : null)
                .timestamp(Instant.now())
                .riskScore(totalScore)
                .riskLevel(riskLevel)
                .reasons(new ArrayList<>(reasons))
                .build();

        log.info("Risk evaluation complete: score={}, level={}, reasons={}", 
                totalScore, riskLevel, riskEvent.getReasons());

        userBehaviorStateService.recordEvent(loginEvent);
        return riskEvent;
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score >= 90) return RiskLevel.CRITICAL;
        if (score >= 70) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}
