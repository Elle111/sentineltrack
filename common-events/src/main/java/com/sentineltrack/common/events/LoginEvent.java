package com.sentineltrack.common.events;

import com.sentineltrack.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginEvent {
    private String eventId;
    private String userId;
    private String sessionId;
    private Instant timestamp;
    private EventType eventType;
    private String ipAddress;
    private String country;
    private String city;
    private String deviceId;
    private String userAgent;
    private boolean success;
}
