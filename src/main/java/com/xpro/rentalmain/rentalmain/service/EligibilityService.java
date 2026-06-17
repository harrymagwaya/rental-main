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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
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

        // TRANSACTION SAFE FIX: Avoid try-catch for expected missing data profiles
        BigDecimal expectedRent = BigDecimal.ZERO;

        // Rewrite this method signature to return an Optional to prevent proxy exceptions
        Optional<PropertyUnitResponse> unitOpt = propertyService.getUnitByTenantOptional(tenantId);

        if (unitOpt.isPresent() && unitOpt.get().rentAmount() != null) {
            expectedRent = unitOpt.get().rentAmount();
            log.info("Active lease profile found. Factoring rent valuation of {} into eligibility calculation.", expectedRent);
        } else {
            log.warn("No active property unit found for tenant: {}. Defaulting rent footprint variables to 0.", tenantId);
        }

        // 2. GATEKEEPER
        if (!control.isCalculationAllowed()) {
            return mapToDeniedResponse(riskDto, "Calculation blocked by Administrator.");
        }

        // 3. CALCULATE FOOTPRINT
        BigDecimal footprint = calculateFootprint(capacity, expectedRent);
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
    public Eligibility getEntityById(UUID eligibilityId) {
        log.info("Fetching eligibility entity directly by ID: {}", eligibilityId);
        return controlRepo.findById(eligibilityId)
                .orElseThrow(() -> new RuntimeException("Eligibility record not found for ID: " + eligibilityId));
    }

    @Transactional(readOnly = true)
    public EligibilityResponseDTO getEligibilityByIdAsDto(UUID eligibilityId) {
        // 1. Fetch the control entity by its direct ID
        Eligibility control = getEntityById(eligibilityId);
        UUID tenantId = control.getTenantId();

        // 2. Hydrate the remaining financial fields using the linked tenant ID
        RiskScoreResponseDTO riskDto = riskService.getLatestScore(tenantId);
        TenantCapacity capacity = capacityRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Financial capacity data missing for tenant: " + tenantId));

        BigDecimal approximateFootprint = (control.getCurrentMaxLimit() != null)
                ? control.getCurrentMaxLimit().multiply(new BigDecimal("2"))
                : BigDecimal.ZERO;

        // 3. Return a clean, production-ready response payload
        return new EligibilityResponseDTO(
                tenantId,
                riskDto.creditScore(),
                riskDto.riskBand(),
                riskDto.riskCategory(),
                capacity.getMonthlyIncome(),
                approximateFootprint,
                control.getCurrentMinLimit(),
                control.getCurrentMaxLimit(),
                control.isCalculationAllowed(),
                generateStatusMessage(control.getLastCalculatedBand(), control.getCurrentMaxLimit()),
                control.getLastReviewedAt()
        );
    }

    @Transactional
    public void deleteEligibilityRecord(UUID tenantId) {
        log.warn("Purging eligibility record for tenant: {}", tenantId);
        Eligibility control = controlRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Record not found for tenant: " + tenantId));
        controlRepo.delete(control);
    }

    @Transactional(readOnly = true)
    public Page<Eligibility> getAllEligibilityPaged(Pageable pageable) {
        log.info("Fetching paged eligibility records.");
        return controlRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public EligibilityResponseDTO getLatestEligibility(UUID tenantId) {
        // 1. Fetch the persisted eligibility control parameters
        Eligibility control = controlRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Eligibility records have not been initialized for tenant: " + tenantId));

        // 2. Get the latest score details directly from the evaluation service
        RiskScoreResponseDTO riskDto = riskService.getLatestScore(tenantId);

        // 3. Fetch financial footprint parameters
        TenantCapacity capacity = capacityRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Financial capacity data missing"));

        // 4. TRANSACTION SAFE FIX: Avoid try-catch block by unboxing an Optional
        BigDecimal expectedRent = BigDecimal.ZERO;
        Optional<PropertyUnitResponse> unitOpt = propertyService.getUnitByTenantOptional(tenantId);

        if (unitOpt.isPresent()) {
            PropertyUnitResponse unit = unitOpt.get();
            if (unit.rentAmount() != null) {
                expectedRent = unit.rentAmount();
            }
        } else {
            log.warn("No active property unit found when reading footprint for tenant: {}. Defaulting rent footprint calculation to 0.", tenantId);
        }

        // Calculate real live footprint safely
        BigDecimal realFootprint = calculateFootprint(capacity, expectedRent);

        return new EligibilityResponseDTO(
                tenantId,
                riskDto.creditScore(),
                riskDto.riskBand(),
                riskDto.riskCategory(),
                capacity.getMonthlyIncome(),
                realFootprint,
                control.getCurrentMinLimit(),
                control.getCurrentMaxLimit(),
                control.isCalculationAllowed(),
                generateStatusMessage(riskDto.riskBand(), control.getCurrentMaxLimit()),
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
            case REJECT,UNRATED -> BigDecimal.ZERO;
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
            case GOLD     -> "Good history. Ensure all utilities are reported to reach Platinum.";
            case SILVER   -> "Keep reporting on-time rent payments to increase your limit.";
            case UNRATED  -> "Your profile is newly registered. Link your Mobile Money, rent ledger, or utility accounts to begin calculating your credit score.";
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