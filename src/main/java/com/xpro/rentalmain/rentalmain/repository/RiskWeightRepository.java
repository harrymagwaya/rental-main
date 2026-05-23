package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.RiskWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskWeightRepository extends JpaRepository<RiskWeight, UUID> {
    List<RiskWeight> findAllByIsActiveTrue();
    Optional<RiskWeight> findByFeatureKey(String featureKey);
    boolean existsByFeatureKey(String featureKey);
}