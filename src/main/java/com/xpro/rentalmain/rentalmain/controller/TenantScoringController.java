package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.service.TenantScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
public class TenantScoringController {

    private final TenantScoringService scoringService;

    /**
     * Calculates the current risk score for a tenant.
     * Logic: Finds Active Link -> Loads Features -> Multiplies by Redis Weights.
     */
    @GetMapping("/tenant/{tenantId}")
    public RiskScore calculateScore(@PathVariable UUID tenantId) {
        return scoringService.calculateTenantScore(tenantId);
    }
}