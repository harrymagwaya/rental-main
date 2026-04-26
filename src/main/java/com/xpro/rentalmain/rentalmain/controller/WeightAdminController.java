package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.WeightUpdateRequest;
import com.xpro.rentalmain.rentalmain.entity.RiskWeight;
import com.xpro.rentalmain.rentalmain.service.RiskWeightService;
import com.xpro.rentalmain.rentalmain.component.RiskWeightCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/risk-weights")
public class WeightAdminController {

    private final RiskWeightService weightService;
    private final RiskWeightCache riskWeightCache;

    public WeightAdminController(RiskWeightService weightService, RiskWeightCache riskWeightCache) {
        this.weightService = weightService;
        this.riskWeightCache = riskWeightCache;
    }

    /**
     * Updates multiple weights.
     * Now returns the List of updated entities for the frontend to confirm.
     */
    @PostMapping("/update")
    public List<RiskWeight> updateWeights(
            @RequestBody WeightUpdateRequest request,
            @RequestHeader(value = "X-Actor-ID", defaultValue = "SYSTEM") String actorId) {

        log.info("Admin weight update request received from actor: {}", actorId);

        List<RiskWeight> updatedWeights = weightService.updateWeights(request.getWeights(), actorId);

        return updatedWeights;
    }

    /**
     * Manually triggers a full cache wipe and reload.
     * Useful for troubleshooting or manual overrides.
     */
    @PostMapping("/refresh-cache")
    public String refreshCache() {
        riskWeightCache.clear();
        // The next GET request will lazy-load the weights,
        // or you could call a service method here to re-prime the cache.
        return "Redis cache cleared. It will self-heal on the next scoring request.";
    }
}