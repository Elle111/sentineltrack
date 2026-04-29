package com.sentineltrack.risk.state;

import com.sentineltrack.common.enums.EventType;
import com.sentineltrack.common.events.LoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserBehaviorStateService {

    private final Map<String, Set<String>> knownDevicesByUser = new ConcurrentHashMap<>();
    private final Map<String, LoginEvent> lastSuccessfulLoginByUser = new ConcurrentHashMap<>();
    private final Map<String, List<LoginEvent>> recentFailedLoginsByUser = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> knownCountriesByUser = new ConcurrentHashMap<>();

    public boolean isNewDevice(LoginEvent event) {
        if (event == null) return false;
        String userId = safeTrim(event.getUserId());
        String deviceId = safeTrim(event.getDeviceId());
        if (userId == null || deviceId == null) return false;

        Set<String> knownDevices = knownDevicesByUser.get(userId);
        return knownDevices == null || !knownDevices.contains(deviceId);
    }

    public boolean isNewCountry(LoginEvent event) {
        if (event == null) return false;
        String userId = safeTrim(event.getUserId());
        String country = safeTrim(event.getCountry());
        if (userId == null || country == null) return false;

        Set<String> knownCountries = knownCountriesByUser.get(userId);
        return knownCountries == null || !knownCountries.contains(country);
    }

    public void recordEvent(LoginEvent event) {
        if (event == null) return;
        String userId = safeTrim(event.getUserId());
        if (userId == null) return;

        String deviceId = safeTrim(event.getDeviceId());
        if (deviceId != null) {
            knownDevicesByUser.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(deviceId);
        }

        String country = safeTrim(event.getCountry());
        if (country != null) {
            knownCountriesByUser.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(country);
        }

        if (isSuccessfulLogin(event)) {
            lastSuccessfulLoginByUser.put(userId, event);
        } else if (isFailedLogin(event)) {
            List<LoginEvent> list = recentFailedLoginsByUser.computeIfAbsent(
                    userId,
                    ignored -> Collections.synchronizedList(new ArrayList<>())
            );
            list.add(event);
        }
    }

    public LoginEvent getLastSuccessfulLogin(String userId) {
        String safeUserId = safeTrim(userId);
        if (safeUserId == null) return null;
        return lastSuccessfulLoginByUser.get(safeUserId);
    }

    public long countRecentFailedLogins(String userId, Duration window) {
        String safeUserId = safeTrim(userId);
        if (safeUserId == null || window == null) return 0;

        List<LoginEvent> list = recentFailedLoginsByUser.get(safeUserId);
        if (list == null) return 0;

        Instant cutoff = Instant.now().minus(window);
        synchronized (list) {
            return list.stream()
                    .map(LoginEvent::getTimestamp)
                    .filter(ts -> ts != null && ts.isAfter(cutoff))
                    .count();
        }
    }

    public void cleanupOldFailedLogins(Duration maxAge) {
        if (maxAge == null) return;
        Instant cutoff = Instant.now().minus(maxAge);

        for (Map.Entry<String, List<LoginEvent>> entry : recentFailedLoginsByUser.entrySet()) {
            List<LoginEvent> list = entry.getValue();
            if (list == null) continue;
            synchronized (list) {
                boolean removed = list.removeIf(e -> e == null || e.getTimestamp() == null || e.getTimestamp().isBefore(cutoff));
                if (removed) {
                    log.debug("Cleaned up old failed logins for userId={}", entry.getKey());
                }
            }
        }
    }

    private static boolean isSuccessfulLogin(LoginEvent event) {
        if (event == null) return false;
        return event.isSuccess() || event.getEventType() == EventType.LOGIN_SUCCESS;
    }

    private static boolean isFailedLogin(LoginEvent event) {
        if (event == null) return false;
        return !event.isSuccess() || event.getEventType() == EventType.LOGIN_FAILURE;
    }

    private static String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

