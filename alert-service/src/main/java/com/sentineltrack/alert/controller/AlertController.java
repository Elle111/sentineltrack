package com.sentineltrack.alert.controller;

import com.sentineltrack.common.events.AlertEvent;
import com.sentineltrack.common.events.RiskEvent;
import com.sentineltrack.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/create")
    public ResponseEntity<AlertEvent> createAlert(@RequestBody RiskEvent riskEvent) {
        log.info("Test endpoint called with risk event: eventId={}, userId={}, riskLevel={}",
                riskEvent.getEventId(),
                riskEvent.getUserId(),
                riskEvent.getRiskLevel());

        return alertService.createAlert(riskEvent)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
