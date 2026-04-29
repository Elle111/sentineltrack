package com.sentineltrack.dashboard.dto;

import com.sentineltrack.common.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String sessionId;
    private String userId;
    private String ipAddress;
    private String country;
    private String city;
    private Instant startTime;
    private Instant lastUpdated;
    private boolean active;
    private int riskScore;
    private RiskLevel riskLevel;
}
