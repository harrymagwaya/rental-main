package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.EligibilityResponseDTO;
import com.xpro.rentalmain.rentalmain.entity.Eligibility;
import com.xpro.rentalmain.rentalmain.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    // =========================================================================
    // CORE ASSESSMENT ENDPOINT
    // =========================================================================

    /**
     * POST /api/v1/eligibility/tenant/{tenantId}/assess
     * Triggers the complete calculation pipeline: scores behavioral features,
     * cross-checks financial capacity against expected rent, and issues credit limits.
     */
    @PostMapping("/tenant/{tenantId}/assess")
    public EligibilityResponseDTO assessTenantEligibility(@PathVariable UUID tenantId) {
        log.info("REST request to run full eligibility assessment for tenant: {}", tenantId);
        return eligibilityService.processFullEligibility(tenantId);
    }

    /**
     * GET /api/v1/eligibility/tenant/{tenantId}/latest
     * Reads the latest cached eligibility snapshot for a specific tenant.
     */
    @GetMapping("/tenant/{tenantId}/latest")
    public EligibilityResponseDTO getLatestTenantEligibility(@PathVariable UUID tenantId) {
        log.info("REST request to fetch latest eligibility for tenant: {}", tenantId);
        return eligibilityService.getLatestEligibility(tenantId);
    }

    /**
     * GET /api/v1/eligibility/{id}
     * Reads a complete, hydrated Eligibility DTO using the direct record ID.
     */
    @GetMapping("/{id}")
    public EligibilityResponseDTO getEligibilityById(@PathVariable UUID id) {
        log.info("REST request to fetch eligibility DTO by record ID: {}", id);
        return eligibilityService.getEligibilityByIdAsDto(id);
    }

    /**
     * GET /api/v1/eligibility/raw/{id}
     * Reads the raw administrative control entity by its direct record ID.
     */
    @GetMapping("/raw/{id}")
    public Eligibility getRawEntityById(@PathVariable UUID id) {
        log.info("REST request to fetch raw eligibility entity by ID: {}", id);
        return eligibilityService.getEntityById(id);
    }

    /**
     * GET /api/v1/eligibility/paged
     * Reads paged records optimized for administrative UI dashboards.
     */
    @GetMapping("/paged")
    public Page<Eligibility> getAllRecordsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch paged eligibility records. Page: {}, Size: {}", page, size);
        return eligibilityService.getAllEligibilityPaged(PageRequest.of(page, size));
    }



    /**
     * DELETE /api/v1/eligibility/tenant/{tenantId}
     * Purges an eligibility record out of the database workspace.
     */
    @DeleteMapping("/tenant/{tenantId}")
    public void purgeEligibilityProfile(@PathVariable UUID tenantId) {
        log.warn("REST request to permanently delete eligibility profile for tenant: {}", tenantId);
        eligibilityService.deleteEligibilityRecord(tenantId);
    }
}