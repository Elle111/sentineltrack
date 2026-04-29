package com.sentineltrack.dashboard.controller;

import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
public class RiskController {

    private final DashboardService dashboardService;

    @GetMapping
    public List<RiskEvent> getAllRiskEvents() {
        return dashboardService.getRecentRiskEvents();
    }
}
