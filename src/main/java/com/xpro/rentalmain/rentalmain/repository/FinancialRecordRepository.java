package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.FinancialRecord;
import com.xpro.rentalmain.rentalmain.model.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    /**
     * 1. THE "TIME-WINDOW" QUERY
     * Used for: "Last 7 days", "Last 30 days", "This Year".
     * Finds all records from a starting point up to RIGHT NOW.
     */
    List<FinancialRecord> findByTenantIdAndTransactionDateAfterOrderByTransactionDateDesc(
            UUID tenantId, LocalDateTime startDate);

    /**
     * 2. THE "ARCHIVE" QUERY
     * Used for: "March 2026", "October 2025".
     * Finds all records within a strictly defined month or year box.
     */
    List<FinancialRecord> findByTenantIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            UUID tenantId, LocalDateTime start, LocalDateTime end);

    /**
     * 3. THE SCORING QUERY
     * Used by EligibilityService to count how many times a category was recorded.
     */
    long countByTenantIdAndCategoryAndTransactionDateAfter(
            UUID tenantId, FinancialCategory category, LocalDateTime date);

    /**
     * 4. THE DUPLICATE CHECK
     * Prevents re-submission of the same MoMo/Bank Txn ID.
     */
    boolean existsByTxnId(String txnId);

    /**
     * 5. FULL HISTORY
     * Standard view for all records ever submitted by the tenant.
     */
    List<FinancialRecord> findByTenantIdOrderByTransactionDateDesc(UUID tenantId);
}