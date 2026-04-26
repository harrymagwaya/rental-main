package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.component.RiskWeightCache;
import com.xpro.rentalmain.rentalmain.entity.RiskWeight;
import com.xpro.rentalmain.rentalmain.service.RiskWeightService;
import com.xpro.rentalmain.rentalmain.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/weights")
@RequiredArgsConstructor
public class RiskWeightController {

    private final RiskWeightService weightService;

    private final RiskWeightCache riskWeightCache;

    // 1. Fetch all weights for the dashboard table
    @GetMapping
    public List<RiskWeight> getAll() {
        return weightService.getAllWeights();
    }

    // 2. Fetch a specific weight by ID
    @GetMapping("/{id}")
    public RiskWeight getOne(@PathVariable UUID id) {
        return weightService.getWeightById(id);
    }

    // 3. Create or Update a single weight
    // Request Param 'actorId' would typically come from your Security Principal
    @PutMapping("/{key}")
    public RiskWeight update(
            @PathVariable String key,
            @RequestParam BigDecimal value,
            @RequestHeader(Constants.ACTOR_ID) UUID actorId) {
        return weightService.updateWeight(key, value, actorId);
    }

    // 4. Bulk update (Useful for a "Save All" button on a settings page)
    @PostMapping("/bulk-update")
    public List<RiskWeight> bulkUpdate(
            @RequestBody Map<String, BigDecimal> newWeights,
            @RequestHeader(Constants.ACTOR_ID) UUID actorId) {
        return weightService.updateWeights(newWeights, actorId);
    }

    // 5. Soft Delete / Deactivate
    @PatchMapping("/{id}/status")
    public RiskWeight toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean active,  @RequestHeader(Constants.ACTOR_ID) UUID actorId) {
        return weightService.toggleWeightStatus(id, active);
    }

    // 6. Hard Delete
    @DeleteMapping("/{id}")
    public void remove(@PathVariable UUID id,  @RequestHeader(Constants.ACTOR_ID) UUID actorId) {
        weightService.deleteWeight(id);
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
