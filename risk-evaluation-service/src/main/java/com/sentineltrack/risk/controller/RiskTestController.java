package com.sentineltrack.risk.controller;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.risk.service.RiskEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/risk")
@RequiredArgsConstructor
public class RiskTestController {

    private final RiskEvaluationService riskEvaluationService;

    @PostMapping("/evaluate")
    public RiskEvent evaluate(@RequestBody LoginEvent event) {
        return riskEvaluationService.evaluateRisk(event);
    }
}

