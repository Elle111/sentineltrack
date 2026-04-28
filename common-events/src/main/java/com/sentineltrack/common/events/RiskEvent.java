package com.sentineltrack.common.events;

import com.sentineltrack.common.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvent {
    private String eventId;
    private String sourceEventId;
    private String userId;
    private String sessionId;
    private Instant timestamp;
    private int riskScore;
    private RiskLevel riskLevel;
    private List<String> reasons;
}
