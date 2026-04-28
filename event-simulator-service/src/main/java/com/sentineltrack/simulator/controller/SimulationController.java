package com.sentineltrack.simulator.controller;

import com.sentineltrack.simulator.service.EventSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulate")
@RequiredArgsConstructor
public class SimulationController {

    private final EventSimulationService eventSimulationService;

    @PostMapping("/login-normal")
    public ResponseEntity<String> simulateNormalLogin() {
        eventSimulationService.simulateNormalLogin();
        return ResponseEntity.ok("Normal login event simulated");
    }

    @PostMapping("/login-suspicious")
    public ResponseEntity<String> simulateSuspiciousLogin() {
        eventSimulationService.simulateSuspiciousLogin();
        return ResponseEntity.ok("Suspicious login event simulated");
    }

    @PostMapping("/failed-logins")
    public ResponseEntity<String> simulateFailedLogins() {
        eventSimulationService.simulateFailedLogins();
        return ResponseEntity.ok("Failed login events simulated");
    }

    @PostMapping("/bulk-normal")
    public ResponseEntity<String> simulateBulkNormal(@RequestParam(defaultValue = "10") int count) {
        eventSimulationService.simulateBulkNormal(count);
        return ResponseEntity.ok("Bulk normal login events simulated");
    }

    @PostMapping("/bulk-suspicious")
    public ResponseEntity<String> simulateBulkSuspicious(@RequestParam(defaultValue = "10") int count) {
        eventSimulationService.simulateBulkSuspicious(count);
        return ResponseEntity.ok("Bulk suspicious login events simulated");
    }
}
