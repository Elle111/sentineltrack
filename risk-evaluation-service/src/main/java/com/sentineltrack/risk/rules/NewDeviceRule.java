package com.sentineltrack.risk.rules;

import com.sentineltrack.common.events.LoginEvent;
import com.sentineltrack.risk.state.UserBehaviorStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewDeviceRule implements RiskRule {

    private final UserBehaviorStateService userBehaviorStateService;

    @Override
    public int evaluate(LoginEvent event) {
        if (event == null) return 0;
        if (!event.isSuccess()) return 0;
        if (isBlank(event.getUserId()) || isBlank(event.getDeviceId())) return 0;

        boolean isNewDevice = userBehaviorStateService.isNewDevice(event);
        log.debug("Evaluating new device rule for userId={}, deviceId={}, isNewDevice={}",
                event.getUserId(), event.getDeviceId(), isNewDevice);

        return isNewDevice ? 25 : 0;
    }

    @Override
    public String reason() {
        return "Login from a new device";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
