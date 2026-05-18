package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.EligibilityResponseDTO;
import com.xpro.rentalmain.rentalmain.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    @GetMapping("/{tenantId}")
    public EligibilityResponseDTO getTenantEligibility(@PathVariable UUID tenantId) {
        return eligibilityService.getLatestEligibility(tenantId);
    }

//    @PostMapping("/{tenantId}/refresh")
//    public EligibilityResponseDTO refreshEligibility(@PathVariable UUID tenantId) {
//        return eligibilityService.processFullEligibility(tenantId);
//    }
}