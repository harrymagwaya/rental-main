package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.component.RiskWeightCache;
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

    /**
     * Updates a single weight, logs the actor, and fires a sync event.
     */
    @Transactional
    public RiskWeight updateWeight(String key, BigDecimal val, UUID actorId) {
        log.info("Actor [{}] is updating weight [{}] to [{}]", actorId, key, val);

        RiskWeight weight = repository.findByFeatureKey(key).orElseGet(RiskWeight::new);
        weight.setFeatureKey(key);
        weight.setWeightValue(val);
        weight.setActive(true);

        RiskWeight saved = repository.save(weight);

        // Fire the event (Contains only the ID, the listener will pull the fresh data)
        eventPublisher.publishEvent(new WeightUpdatedEvent(saved.getId()));

        return saved;
    }

    /**
     * Bulk update method if your frontend still sends a Map of weights.
     */
    @Transactional
    public List<RiskWeight> updateWeights(Map<String, BigDecimal> newWeights, UUID actorId) {
        return newWeights.entrySet().stream()
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