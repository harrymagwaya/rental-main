package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.component.RiskWeightCache;
import com.xpro.rentalmain.rentalmain.dto.RiskWeightCreateRequest;
import com.xpro.rentalmain.rentalmain.dto.RiskWeightUpdateRequest;
import com.xpro.rentalmain.rentalmain.entity.RiskWeight;
import com.xpro.rentalmain.rentalmain.event.WeightUpdatedEvent;
import com.xpro.rentalmain.rentalmain.repository.RiskWeightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RiskWeightService {

    private final RiskWeightRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final RiskWeightCache riskWeightCache;

    public RiskWeightService(RiskWeightRepository repository,
                             ApplicationEventPublisher eventPublisher,
                             RiskWeightCache riskWeightCache) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.riskWeightCache = riskWeightCache;
    }

    @Transactional
    public RiskWeight createWeight(RiskWeightCreateRequest request) {
        log.info("Creating new weight: {}", request.featureKey());

        // Check if a weight with this key already exists to prevent duplicates
        repository.findByFeatureKey(request.featureKey()).ifPresent(w -> {
            throw new RuntimeException("Weight with key " + request.featureKey() + " already exists. Use Update.");
        });

        RiskWeight weight = new RiskWeight();
        weight.setFeatureKey(request.featureKey());
        weight.setWeightValue(request.weightValue());
        weight.setActive(true);

        RiskWeight saved = repository.save(weight);
        eventPublisher.publishEvent(new WeightUpdatedEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public RiskWeight updateWeight(UUID weightId, RiskWeightUpdateRequest request, UUID actorId) {
        log.info("Actor [{}] is updating weight ID: {}", actorId, weightId);

        // Use the weightId from the PathVariable/Method arg for maximum security
        RiskWeight weight = repository.findById(weightId)
                .orElseThrow(() -> new RuntimeException("Cannot update. Weight ID not found: " + weightId));

        if (request.weightValue() != null) {
            weight.setWeightValue(request.weightValue());
        }

        if (request.active() != null) {
            weight.setActive(request.active());
        }

        RiskWeight saved = repository.save(weight);

        // Sync to Redis
        eventPublisher.publishEvent(new WeightUpdatedEvent(saved.getId()));
        return saved;
    }

    /**
     * Bulk update for the dashboard.
     * Since we need IDs for bulk, we can use a Map where the Key is the UUID.
     */
    @Transactional
    public List<RiskWeight> updateBulkWeights(Map<UUID, RiskWeightUpdateRequest> updates, UUID actorId) {
        return updates.entrySet().stream()
                .map(entry -> updateWeight(entry.getKey(), entry.getValue(), actorId))
                .collect(Collectors.toList());
    }

    /**
     * Fetch a specific weight using the Cache Component (Lazy-loads if empty).
     */
    public BigDecimal getActiveWeight(String featureKey) {
        return riskWeightCache.getWeight(featureKey);
    }

    @Transactional(readOnly = true)
    public List<RiskWeight> getAllWeights() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public RiskWeight getWeightById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weight configuration not found: " + id));
    }

    @Transactional
    public void deleteWeight(UUID id) {
        repository.deleteById(id);
        log.warn("Weight [{}] was permanently deleted.", id);
        // Note: You might want to fire a 'WeightDeletedEvent' to clear Redis!
    }

    /**
     * Soft Delete: Keeps the record but the scoring engine will ignore it.
     */
    @Transactional
    public RiskWeight toggleWeightStatus(UUID id, boolean active) {
        RiskWeight weight = repository.findById(id).orElseThrow();
        weight.setActive(active);
        RiskWeight saved = repository.save(weight);

        // Notify cache to refresh
        eventPublisher.publishEvent(new WeightUpdatedEvent(saved.getId()));
        return saved;
    }
}