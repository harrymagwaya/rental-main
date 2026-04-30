package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.RiskScoreResponseDTO;
import com.xpro.rentalmain.rentalmain.entity.CreditScore;
import com.xpro.rentalmain.rentalmain.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoringController {

    private final RiskCalculationService calculationService;

    /**
     * POST /api/v1/scores/generate/{tenantId}
     * Triggers the full calculation engine and saves the result to the DB.
     */
    @PostMapping("/generate/{tenantId}")
    public RiskScoreResponseDTO generateScore(@PathVariable UUID tenantId) {
        // This triggers the service -> which triggers the model -> which hits Redis.
        return calculationService.generateScore(tenantId);
    }

    /**
     * Returns the last saved result from the database.
     */
    @GetMapping("/latest/{tenantId}")
    public RiskScoreResponseDTO getLatest(@PathVariable UUID tenantId) {
        return calculationService.getLatestScore(tenantId);
    }
}