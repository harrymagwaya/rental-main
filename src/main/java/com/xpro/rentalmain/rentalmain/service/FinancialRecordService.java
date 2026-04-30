package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.FinancialRecordRequest;
import com.xpro.rentalmain.rentalmain.dto.FinancialRecordResponse;
import com.xpro.rentalmain.rentalmain.entity.FinancialRecord;
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;
import com.xpro.rentalmain.rentalmain.repository.FinancialRecordRepository;
import com.xpro.rentalmain.rentalmain.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepo;
    private final TenantRepository tenantRepository;

    /**
     * CREATE: Attaches a new manual financial entry to a tenant.
     */
    @Transactional
    public FinancialRecordResponse attachRecordToTenant(FinancialRecordRequest request) {
        log.info("Attaching record for tenant: {}", request.tenantId());

        if (!tenantRepository.existsById(request.tenantId())) {
            throw new EntityNotFoundException("Tenant not found with ID: " + request.tenantId());
        }

        if (recordRepo.existsByTxnId(request.txnId())) {
            throw new IllegalArgumentException("Transaction ID " + request.txnId() + " already exists.");
        }

        FinancialRecord record = FinancialRecord.builder()
                .tenantId(request.tenantId())
                .txnId(request.txnId())
                .category(request.category())
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .status(PaymentStatus.PENDING) // New records always start as PENDING
                .referenceNote(request.referenceNote())
                .build();

        return mapToResponse(recordRepo.save(record));
    }

    /**
     * READ: Get single record details.
     */
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(UUID recordId) {
        return recordRepo.findById(recordId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Record not found: " + recordId));
    }

    /**
     * READ: Get full history for a tenant.
     */
    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getTenantHistory(UUID tenantId) {
        return recordRepo.findByTenantIdOrderByTransactionDateDesc(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * UPDATE: Change details (Amount/Date/Note).
     * Only allowed if the status is PENDING.
     */
    @Transactional
    public FinancialRecordResponse updateRecord(UUID recordId, FinancialRecordRequest request) {
        FinancialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Record not found: " + recordId));

        // GUARD: If it's already verified (ON_TIME, LATE, etc.), it's locked.
        if (record.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot update a record once it has moved past PENDING status.");
        }

        record.setAmount(request.amount());
        record.setReferenceNote(request.referenceNote());
        record.setTransactionDate(request.transactionDate());

        return mapToResponse(recordRepo.save(record));
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getTenantHistoryByTimeframe(UUID tenantId, String timeframe) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        startTime = switch (timeframe.toLowerCase()) {
            case "7days" -> now.minusDays(7);
            case "30days" -> now.minusDays(30);
            case "month" -> now.withDayOfMonth(1).withHour(0).withMinute(0); // Start of current month
            case "year" -> now.withDayOfYear(1).withHour(0).withMinute(0);  // Start of current year
            default -> now.minusDays(30); // Default to last 30 days
        };

        log.info("Fetching history for tenant {} since {}", tenantId, startTime);

        return recordRepo.findByTenantIdAndTransactionDateAfterOrderByTransactionDateDesc(tenantId, startTime)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getHistoryForSpecificMonth(UUID tenantId, int year, int month) {
        // 1. Calculate the start: e.g., 2026-03-01T00:00
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);

        // 2. Calculate the end: e.g., 2026-03-31T23:59:59
        // .lengthOfMonth() handles Feb 28/29 and 30/31 day months automatically
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        log.info("Fetching history for tenant {} for the period: {} to {}", tenantId, startOfMonth, endOfMonth);

        return recordRepo.findByTenantIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        tenantId, startOfMonth, endOfMonth)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * PATCH: Specifically for Admin/Landlord to move status from PENDING to ON_TIME/LATE/etc.
     */
    @Transactional
    public FinancialRecordResponse updateStatus(UUID recordId, PaymentStatus newStatus) {
        FinancialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Record not found"));

        log.info("Transitioning record {} status to {}", recordId, newStatus);
        record.setStatus(newStatus);
        return mapToResponse(recordRepo.save(record));
    }

    /**
     * DELETE: Remove a record.
     * Logic: Only PENDING or GRACE_PERIOD records should be deletable by users.
     */
    @Transactional
    public void deleteRecord(UUID recordId) {
        FinancialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Record not found"));

        // Protect finalized scoring data
        if (record.getStatus() == PaymentStatus.ON_TIME || record.getStatus() == PaymentStatus.LATE) {
            throw new IllegalStateException("Verified payment history (ON_TIME/LATE) cannot be deleted.");
        }

        recordRepo.delete(record);
        log.info("Successfully deleted financial record: {}", recordId);
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord entity) {
        return new FinancialRecordResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTxnId(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getStatus(),
                entity.getReferenceNote()
        );
    }
}