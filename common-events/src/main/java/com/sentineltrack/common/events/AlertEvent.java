package com.sentineltrack.common.events;

import com.sentineltrack.common.enums.AlertSeverity;
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
public class AlertEvent {
    private String alertId;
    private String riskEventId;
    private String userId;
    private String sessionId;
    private Instant timestamp;
    private AlertSeverity severity;
    private String message;
    private int riskScore;
    private List<String> reasons;
}
