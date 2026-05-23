package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO;
import com.xpro.rentalmain.rentalmain.repository.TenantFeatureLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Applies global read-only optimization to all methods
public class TenantFeatureHistoryService {

    private final TenantFeatureLinkRepository linkRepo;

    /**
     * Pulls the complete historical trend list for a tenant to feed frontend charts.
     */
    public List<TenantFeatureHistoryDTO> getHistoryByTenant(UUID tenantId) {
        log.info("Fetching database-ingested feature timeline for tenant: {}", tenantId);
        return linkRepo.findFeatureHistoryByTenantId(tenantId);
    }

    /**
     * Inspects a specific historical snapshot milestone by its unique link ID.
     */
    public TenantFeatureHistoryDTO getByLinkId(UUID linkId) {
        log.info("Fetching specific snapshot profile details for link ID: {}", linkId);
        return linkRepo.findFeatureHistoryByLinkId(linkId)
                .orElseThrow(() -> new RuntimeException("Snapshot timeline record matching ID not found: " + linkId));
    }

    /**
     * Pulls a paginated global historical timeline across all tenants in the system.
     */
    public org.springframework.data.domain.Page<TenantFeatureHistoryDTO> getAllHistory(int page, int size) {
        log.info("Fetching global system snapshot history timeline. Page: {}, Size: {}", page, size);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return linkRepo.findAllFeatureHistory(pageable);
    }
}