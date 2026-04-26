package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BehavioralFeaturesRepository extends JpaRepository<BehavioralFeatures, UUID> {
}
