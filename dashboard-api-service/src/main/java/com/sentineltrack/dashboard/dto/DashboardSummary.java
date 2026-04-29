package com.sentineltrack.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private int totalSessions;
    private int totalAlerts;
    private int highRiskSessions;
    private int criticalAlerts;
    private int mediumRiskEvents;
    private int highRiskEvents;
    private int criticalRiskEvents;
    private Instant lastUpdated;
}
