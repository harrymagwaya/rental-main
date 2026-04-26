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

}
