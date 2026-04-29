package com.sentineltrack.dashboard.controller;

import com.sentineltrack.dashboard.dto.SessionInfo;
import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final DashboardService dashboardService;

    @GetMapping
    public List<SessionInfo> getAllSessions() {
        return dashboardService.getRecentSessions();
    }
}
