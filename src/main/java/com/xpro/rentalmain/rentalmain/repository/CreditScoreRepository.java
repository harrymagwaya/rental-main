package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditScoreRepository extends JpaRepository<CreditScore, UUID> {
    Optional<CreditScore> findTopByTenantIdOrderByScoredAtDesc(UUID tenantId);

}
