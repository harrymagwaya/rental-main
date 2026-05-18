package com.xpro.rentalmain.rentalmain.scheduler;

import com.xpro.rentalmain.rentalmain.service.TenantService;
import com.xpro.rentalmain.rentalmain.service.TenantScoringOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoringScheduler {

    private final TenantService tenantService; // Now using the service instead of repo
    private final TenantScoringOrchestrator orchestrator;

    /**
     * Runs every 20 minutes: 0, 20, 40 past the hour.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void runFrequentScoreRefresh() {
        log.info("Cron Triggered: Starting 20-minute financial profile refresh.");

        // 1. Fetch IDs via Service (Abstractions are better for testing/maintenance)
        tenantService.getAllActiveTenantIds().forEach(tenantId -> {
            try {
                // 2. Execute the Master Pipeline
                orchestrator.refreshTenantFinancialProfile(tenantId);
            } catch (Exception e) {
                log.error(
                        "Failed to calculate batch score for tenant {}. Reason: {}",
                        tenantId,
                        e.getMessage(),
                        e
                );
            }
        });

        log.info("Cron Completed: 5-minute refresh cycle finished.");
    }
}