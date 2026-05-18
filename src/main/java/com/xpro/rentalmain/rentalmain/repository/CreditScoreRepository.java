package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.CreditScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditScoreRepository extends JpaRepository<CreditScore, UUID> {
    Optional<CreditScore> findTopByTenantIdOrderByScoredAtDesc(UUID tenantId);

    @Query("SELECT cs FROM CreditScore cs WHERE cs.scoredAt IN " +
            "(SELECT MAX(cs2.scoredAt) FROM CreditScore cs2 GROUP BY cs2.tenantId) " +
            "ORDER BY cs.score DESC")
    List<CreditScore> findAllLatestRanked();

    @Query("SELECT cs FROM CreditScore cs WHERE cs.scoredAt IN " +
            "(SELECT MAX(cs2.scoredAt) FROM CreditScore cs2 GROUP BY cs2.tenantId) " +
            "ORDER BY cs.score DESC")
    Page<CreditScore> findAllLatestRankedPaged(Pageable pageable);

    // For the Dashboard: Get the most recent score for a tenant
    Optional<CreditScore> findFirstByTenantIdOrderByScoredAtDesc(UUID tenantId);

    // For the Dashboard Table: Get latest scores for all tenants
    @Query(value = "SELECT DISTINCT ON (tenant_id) * FROM credit_scores ORDER BY tenant_id, scored_at DESC",
            nativeQuery = true)
    List<CreditScore> findAllLatestScores();

    // For History Charts: Get all scores for a specific tenant
    List<CreditScore> findByTenantIdOrderByScoredAtDesc(UUID tenantId);

}
