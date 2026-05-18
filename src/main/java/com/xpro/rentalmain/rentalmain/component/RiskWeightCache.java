package com.xpro.rentalmain.rentalmain.component;

import com.xpro.rentalmain.rentalmain.event.WeightUpdatedEvent;
import com.xpro.rentalmain.rentalmain.repository.RiskWeightRepository;
import com.xpro.rentalmain.rentalmain.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class RiskWeightCache {

    private final RiskWeightRepository repository;
    private final RMap<String, BigDecimal> redisWeightMap;

    public RiskWeightCache(RedissonClient redissonClient, RiskWeightRepository repository) {
        this.repository = repository;
        // Reference the constant for "risk_weights_cache"
        this.redisWeightMap = redissonClient.getMap(Constants.RISK_WEIGHTS_CACHE);
    }

    // ==========================================
    // 1. THE LISTENER (Proactive Sync)
    // ==========================================
    /**
     * Synchronous listener that refreshes the cache after a DB commit.
     * Only needs the weightId to pull the fresh record.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWeightUpdate(WeightUpdatedEvent event) {
        log.info("DB Commit confirmed. Refreshing cache for Weight ID: {}", event.weightId());
        this.refreshFromDb(event.weightId());
    }

    // ==========================================
    // 2. THE GETTER (User Access + Self-Healing)
    // ==========================================
    /**
     * Always hits Redis first. If the key is missing, it pulls from DB
     * to repopulate the cache (Self-Healing).
     */
    public BigDecimal getWeight(String featureKey) {
        BigDecimal value = redisWeightMap.get(featureKey);

        if (value == null) {
            log.warn("Cache miss for [{}]. Recovering from Database...", featureKey);
            return repository.findByFeatureKey(featureKey)
                    .map(weight -> {
                        redisWeightMap.put(weight.getFeatureKey(), weight.getWeightValue());
                        return weight.getWeightValue();
                    })
                    .orElse(BigDecimal.ZERO);
        }
        return value;
    }

    // ==========================================
    // 3. CACHE MANAGEMENT (Internal/External)
    // ==========================================

    /**
     * Pulls specific record from DB and updates the String key in Redis.
     */
    public void refreshFromDb(UUID weightId) {
        repository.findById(weightId).ifPresent(weight -> {
            redisWeightMap.put(weight.getFeatureKey(), weight.getWeightValue());
            log.debug("Cache updated: {} = {}", weight.getFeatureKey(), weight.getWeightValue());
        });
    }

    /**
     * Removes a specific feature key from Redis.
     */
    public void evict(String featureKey) {
        redisWeightMap.fastRemove(featureKey);
        log.info("Evicted [{}] from Redis cache.", featureKey);
    }

    /**
     * Completely wipes the risk weights cache.
     */
    public void clear() {
        redisWeightMap.clear();
        log.warn("Risk weight cache has been cleared.");
    }
}