package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO;
import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantFeatureLinkRepository extends JpaRepository<TenantFeatureLink, UUID> {

    Optional<TenantFeatureLink> findByTenantIdAndIsActiveTrue(UUID tenantId);

    boolean existsByFeatureSnapshotId(UUID tenantId);

    /**
     * Finds all unique Tenant IDs that have an active feature link.
     * This is the "Master List" for your batch scoring.
     */
    @Query("SELECT DISTINCT f.tenantId FROM TenantFeatureLink f WHERE f.isActive = true")
    List<UUID> findAllActiveTenantIds();

    @Query("""
        SELECT new com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO(
            l.id, l.tenantId, f.id, l.isActive,
            f.rentConsistency, f.utilityPayments, f.airtimeUsage,
            f.savingsConsistency, f.loanRepaymentRate, f.mobileMoneyVolume,
            f.transactionDiversity, f.lengthOfResidence,
            l.createdAt
        )
        FROM TenantFeatureLink l
        JOIN BehavioralFeatures f ON l.featureSnapshotId = f.id
        WHERE l.tenantId = :tenantId
        ORDER BY l.createdAt DESC
    """)
    List<TenantFeatureHistoryDTO> findFeatureHistoryByTenantId(@Param("tenantId") UUID tenantId);

    // Query 2: Fetch one specific item when a user clicks an archive item row
    @Query("""
        SELECT new com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO(
            l.id, l.tenantId, f.id, l.isActive,
            f.rentConsistency, f.utilityPayments, f.airtimeUsage,
            f.savingsConsistency, f.loanRepaymentRate, f.mobileMoneyVolume,
            f.transactionDiversity, f.lengthOfResidence,
            l.createdAt
        )
        FROM TenantFeatureLink l
        JOIN BehavioralFeatures f ON l.featureSnapshotId = f.id
        WHERE l.id = :linkId
    """)
    Optional<TenantFeatureHistoryDTO> findFeatureHistoryByLinkId(@Param("linkId") UUID linkId);

    @Query("""
        SELECT new com.xpro.rentalmain.rentalmain.dto.TenantFeatureHistoryDTO(
            l.id, l.tenantId, f.id, l.isActive,
            f.rentConsistency, f.utilityPayments, f.airtimeUsage,
            f.savingsConsistency, f.loanRepaymentRate, f.mobileMoneyVolume,
            f.transactionDiversity, f.lengthOfResidence,
            l.createdAt
        )
        FROM TenantFeatureLink l
        JOIN BehavioralFeatures f ON l.featureSnapshotId = f.id
        ORDER BY l.createdAt DESC
    """)
    Page<TenantFeatureHistoryDTO> findAllFeatureHistory(Pageable pageable);
}
