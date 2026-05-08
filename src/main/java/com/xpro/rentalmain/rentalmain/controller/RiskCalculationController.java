package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.RiskScoreResponseDTO;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
public class RiskCalculationController {

    private final RiskCalculationService calculationService;

    /**
     * 1. PREVIEW: Just runs the math and returns the result.
     * Does NOT save to the database history.
     */
    @GetMapping("/preview/{tenantId}")
    public RiskScore previewScore(@PathVariable UUID tenantId) {
        return calculationService.calculateTenantRiskScore(tenantId);
    }

    /**
     * 2. GENERATE: Runs the math AND saves the result to the DB.
     * This creates a new entry in the credit_scores table.
     */
    @PostMapping("/generate/{tenantId}")
    public RiskScoreResponseDTO generateAndSave(@PathVariable UUID tenantId) {
        return calculationService.generateScore(tenantId);
    }

    /**
     * 3. LATEST: Fetches the most recent saved score for a specific tenant.
     */
    @GetMapping("/latest/{tenantId}")
    public RiskScoreResponseDTO getLatestForTenant(@PathVariable UUID tenantId) {
        return calculationService.getLatestScore(tenantId);
    }

    /**
     * GET /api/v1/scoring/ranked-list?page=0&size=10
     * Returns a paginated ranked list.
     */
    @GetMapping("/ranked-list")
    public Page<RiskScoreResponseDTO> getRankedList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return calculationService.getRankedLeaderboardPaged(page, size);
    }

}