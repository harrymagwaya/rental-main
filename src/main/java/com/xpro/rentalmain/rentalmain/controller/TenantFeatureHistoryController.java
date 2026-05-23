package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO;
import com.xpro.rentalmain.rentalmain.service.TenantFeatureHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenant-feature-link")
@RequiredArgsConstructor
public class TenantFeatureHistoryController {

    private final TenantFeatureHistoryService historyService;

    // Endpoint 1: Load the full historical dashboard timeline list
    @GetMapping("/tenant/{tenantId}")
    @ResponseStatus(HttpStatus.OK)
    public List<TenantFeatureHistoryDTO> getTenantHistory(@PathVariable UUID tenantId) {
        return historyService.getHistoryByTenant(tenantId);
    }

    // Endpoint 2: Drill down into one single historical index node item
    @GetMapping("/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public TenantFeatureHistoryDTO getById(@PathVariable UUID linkId) {
        return historyService.getByLinkId(linkId);
    }

    // Endpoint 3: Load the entire system global timeline for all tenants (Paginated)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public org.springframework.data.domain.Page<TenantFeatureHistoryDTO> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return historyService.getAllHistory(page, size);
    }
}