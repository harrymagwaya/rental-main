package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.TenantCapacityRequestDTO;
import com.xpro.rentalmain.rentalmain.entity.TenantCapacity;
import com.xpro.rentalmain.rentalmain.repository.TenantCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCapacityService {

    private final TenantCapacityRepository capacityRepo;

    @Transactional
    public TenantCapacity createOrUpdate(TenantCapacityRequestDTO dto) {
        // Check if a record already exists for this tenant
        return capacityRepo.findByTenantId(dto.tenantId())
                .map(existing -> updateExisting(existing, dto))
                .orElseGet(() -> createNew(dto));
    }

    private TenantCapacity createNew(TenantCapacityRequestDTO dto) {
        log.info("Creating new capacity profile for tenant: {}", dto.tenantId());
        return capacityRepo.save(TenantCapacity.builder()
                .tenantId(dto.tenantId())
                .monthlyIncome(dto.monthlyIncome())
                .avgMomoVolume(dto.avgMomoVolume())
                .avgUtilitySpend(dto.avgUtilitySpend())
                .avgSavingsDeposit(dto.avgSavingsDeposit())
                .avgAirtimeSpend(dto.avgAirtimeSpend())
                .isVerified(dto.isVerified() != null && dto.isVerified())
                .build());
    }

    @Transactional
    public void initializeShell(UUID tenantId) {
        if (capacityRepo.findByTenantId(tenantId).isEmpty()) {
            log.info("Initializing financial shell for tenant: {}", tenantId);

            TenantCapacity shell = TenantCapacity.builder()
                    .tenantId(tenantId)
                    // Initialize with zero/null safe values
                    .monthlyIncome(BigDecimal.ZERO)
                    .avgMomoVolume(BigDecimal.ZERO)
                    .avgUtilitySpend(BigDecimal.ZERO)
                    .avgSavingsDeposit(BigDecimal.ZERO)
                    .avgAirtimeSpend(BigDecimal.ZERO)
                    .isVerified(false)
                    .build();

            capacityRepo.save(shell);
        }
    }

    private TenantCapacity updateExisting(TenantCapacity existing, TenantCapacityRequestDTO dto) {
        log.info("Updating existing capacity profile for tenant: {}", dto.tenantId());

        // Null-safe updates: Only change the field if the DTO value is provided
        if (dto.monthlyIncome() != null) existing.setMonthlyIncome(dto.monthlyIncome());
        if (dto.avgMomoVolume() != null) existing.setAvgMomoVolume(dto.avgMomoVolume());
        if (dto.avgUtilitySpend() != null) existing.setAvgUtilitySpend(dto.avgUtilitySpend());
        if (dto.avgSavingsDeposit() != null) existing.setAvgSavingsDeposit(dto.avgSavingsDeposit());
        if (dto.avgAirtimeSpend() != null) existing.setAvgAirtimeSpend(dto.avgAirtimeSpend());
        if (dto.isVerified() != null) existing.setVerified(dto.isVerified());

        return capacityRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public TenantCapacity getByTenant(UUID tenantId) {
        return capacityRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("No capacity profile found for tenant: " + tenantId));
    }
    /**
     * Retrieves all tenant capacity profiles.
     */
    @Transactional(readOnly = true)
    public List<TenantCapacity> getAll() {
        log.info("Fetching all tenant capacity profiles");
        return capacityRepo.findAll();
    }

    /**
     * Finds a capacity record by its unique database ID (UUID).
     */
    @Transactional(readOnly = true)
    public TenantCapacity getById(UUID id) {
        log.info("Fetching capacity profile by ID: {}", id);
        return capacityRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Capacity record not found for ID: " + id));
    }

    /**
     * Deletes a capacity record by its ID.
     */
    @Transactional
    public void delete(UUID id) {
        log.warn("Deleting tenant capacity record with ID: {}", id);
        if (!capacityRepo.existsById(id)) {
            throw new RuntimeException("Cannot delete: Record not found for ID: " + id);
        }
        capacityRepo.deleteById(id);
    }
}