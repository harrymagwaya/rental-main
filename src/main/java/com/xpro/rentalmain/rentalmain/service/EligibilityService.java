package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.EligibilityResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.PropertyUnitResponse;
import com.xpro.rentalmain.rentalmain.dto.RiskScoreResponseDTO;
import com.xpro.rentalmain.rentalmain.repository.TenantCapacityRepository;
import com.xpro.rentalmain.rentalmain.repository.EligibilityRepository;
import com.xpro.rentalmain.rentalmain.repository.PropertyUnitRepository; // Added
import com.xpro.rentalmain.rentalmain.entity.Eligibility;
import com.xpro.rentalmain.rentalmain.entity.PropertyUnit; // Added
import com.xpro.rentalmain.rentalmain.entity.TenantCapacity;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final TenantCapacityRepository capacityRepo;
    private final EligibilityRepository controlRepo;
    private final RiskCalculationService riskService;
    private final PropertyService propertyService;

    @Transactional
    public EligibilityResponseDTO processFullEligibility(UUID tenantId) {
        log.info("Aggregating full eligibility for tenant: {}", tenantId);

        // 1. GATHER DATA
        RiskScoreResponseDTO riskDto = riskService.generateScore(tenantId);

        TenantCapacity capacity = capacityRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Financial capacity data missing"));

        Eligibility control = controlRepo.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultControl(tenantId));

        // Get the unit to find out how much rent they are supposed to pay
        PropertyUnitResponse unit = propertyService.getUnitByTenant(tenantId);

        // 2. GATEKEEPER
        if (!control.isCalculationAllowed()) {
            return mapToDeniedResponse(riskDto, "Calculation blocked by Administrator.");
        }

        // 3. CALCULATE FOOTPRINT (Now using Expected Rent)
        BigDecimal footprint = calculateFootprint(capacity, unit.rentAmount());
        BigDecimal maxLimit = calculateMaxLimit(footprint, riskDto.riskBand());

        // 4. PERSIST
        updateControlEntity(control, maxLimit, riskDto.riskBand());

        // 5. RESPOND
        return new EligibilityResponseDTO(
                tenantId,
                riskDto.creditScore(),
                riskDto.riskBand(),
                riskDto.riskCategory(),
                capacity.getMonthlyIncome(),
                footprint,
                control.getCurrentMinLimit(),
                control.getCurrentMaxLimit(),
                control.isCalculationAllowed(),
                generateStatusMessage(riskDto.riskBand(), maxLimit),
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public EligibilityResponseDTO getLatestEligibility(UUID tenantId) {
        // 1. Fetch the persisted eligibility record from the DB
        Eligibility control = controlRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Eligibility not yet calculated for tenant: " + tenantId));

        // 2. Get the latest score details to fill the DTO
        RiskScoreResponseDTO riskDto = riskService.getLatestScore(tenantId);

        TenantCapacity capacity = capacityRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Financial capacity data missing"));

        BigDecimal approximateFootprint = (control.getCurrentMaxLimit() != null)
                ? control.getCurrentMaxLimit().multiply(new BigDecimal("2"))
                : BigDecimal.ZERO;

        return new EligibilityResponseDTO(
                tenantId,
                riskDto.creditScore(),
                riskDto.riskBand(),
                riskDto.riskCategory(),
                capacity.getMonthlyIncome(), // FIXED: No longer null
                 approximateFootprint,
                control.getCurrentMinLimit(),
                control.getCurrentMaxLimit(),
                control.isCalculationAllowed(),
                generateStatusMessage(control.getLastCalculatedBand(), control.getCurrentMaxLimit()),
                control.getLastReviewedAt()
        );
    }

    private BigDecimal calculateFootprint(TenantCapacity capacity, BigDecimal expectedRent) {
        // Calculate raw disposable income first
        BigDecimal disposableIncome = capacity.getMonthlyIncome().subtract(expectedRent);

        // Safety Check: If rent is higher than income, they have 0 disposable income from salary
        if (disposableIncome.compareTo(BigDecimal.ZERO) < 0) {
            disposableIncome = BigDecimal.ZERO;
            log.warn("Tenant's expected rent is higher than declared income!");
        }

        BigDecimal footprint = disposableIncome;

        // Add the secondary economic signals
        footprint = footprint.add(capacity.getAvgMomoVolume().multiply(new BigDecimal("0.20")));
        footprint = footprint.add(capacity.getAvgUtilitySpend());
        footprint = footprint.add(capacity.getAvgSavingsDeposit());

        return footprint;
    }

    private BigDecimal calculateMaxLimit(BigDecimal footprint, RiskBand band) {
        BigDecimal multiplier = switch (band) {
            case PLATINUM -> new BigDecimal("0.50");
            case GOLD -> new BigDecimal("0.35");
            case SILVER -> new BigDecimal("0.25"); // Increased from 0.20
            case BRONZE -> new BigDecimal("0.10"); // Allow a 10% limit for Bronze instead of ZERO
            case REJECT -> BigDecimal.ZERO;
        };

        return footprint.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    private void updateControlEntity(Eligibility control, BigDecimal maxLimit, RiskBand band) {
        BigDecimal minLimit = maxLimit.multiply(new BigDecimal("0.60")).setScale(0, RoundingMode.HALF_UP);

        control.setCurrentMaxLimit(maxLimit);
        control.setCurrentMinLimit(minLimit);
        control.setLastCalculatedBand(band);
        control.setLastReviewedAt(LocalDateTime.now());

        controlRepo.save(control);
    }

    private String generateStatusMessage(RiskBand band, BigDecimal maxLimit) {
        // Fallback: If they have no limit due to low income or admin block
        if (maxLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return "Improve your payment consistency to unlock a borrowing limit.";
        }

        // Your custom advice mapped to the RiskBand
        return switch (band) {
            case PLATINUM -> "Excellent consistency! You have reached your maximum borrowing power.";
            case GOLD -> "Good history. Ensure all utilities are reported to reach Platinum.";
            case SILVER -> "Keep reporting on-time rent payments to increase your limit.";
            case BRONZE, REJECT -> "Focus on on-time payments to start building your credit profile.";
        };
    }

    private Eligibility createDefaultControl(UUID tenantId) {
        return Eligibility.builder()
                .tenantId(tenantId)
                .isCalculationAllowed(true)
                .build();
    }

    private EligibilityResponseDTO mapToDeniedResponse(RiskScoreResponseDTO riskDto, String message) {
        return new EligibilityResponseDTO(
                riskDto.tenantId(), riskDto.creditScore(), riskDto.riskBand(), riskDto.riskCategory(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, message, LocalDateTime.now()
        );
    }
}