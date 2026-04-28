package com.sentineltrack.simulator.service;

import com.sentineltrack.simulator.producer.LoginEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EventSimulationServiceTest {

    @Mock
    private LoginEventProducer loginEventProducer;

    @InjectMocks
    private EventSimulationService eventSimulationService;

    @Test
    void simulateNormalLogin_publishesOneEvent() {
        eventSimulationService.simulateNormalLogin();

        verify(loginEventProducer, times(1)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateSuspiciousLogin_publishesOneEvent() {
        eventSimulationService.simulateSuspiciousLogin();

        verify(loginEventProducer, times(1)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateFailedLogins_publishesFiveEvents() {
        eventSimulationService.simulateFailedLogins();

        verify(loginEventProducer, times(5)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateBulkNormal_publishesSpecifiedCount() {
        eventSimulationService.simulateBulkNormal(10);

        verify(loginEventProducer, times(10)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateBulkSuspicious_publishesSpecifiedCount() {
        eventSimulationService.simulateBulkSuspicious(10);

        verify(loginEventProducer, times(10)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateBulkNormal_capsAt100() {
        eventSimulationService.simulateBulkNormal(150);

        verify(loginEventProducer, times(100)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void simulateBulkNormal_defaultsTo1WhenLessThan1() {
        eventSimulationService.simulateBulkNormal(0);

        verify(loginEventProducer, times(1)).sendLoginEvent(
                org.mockito.ArgumentMatchers.any()
        );
    }
}
