package com.sentineltrack.dashboard.config;

import com.sentineltrack.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockDataInitializer implements CommandLineRunner {

    private final DashboardService dashboardService;

    @Value("${sentineltrack.dashboard.mock-data-enabled:false}")
    private boolean mockDataEnabled;

    @Override
    public void run(String... args) {
        if (mockDataEnabled) {
            log.info("Initializing mock data for dashboard");
            dashboardService.initializeMockData();
        } else {
            log.info("Mock data initialization is disabled");
        }
    }
}
