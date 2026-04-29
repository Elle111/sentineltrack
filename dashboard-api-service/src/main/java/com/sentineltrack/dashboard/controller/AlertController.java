package com.sentineltrack.dashboard.controller;

import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final DashboardService dashboardService;

    @GetMapping
    public List<AlertEvent> getAllAlerts() {
        return dashboardService.getRecentAlerts();
    }
}
