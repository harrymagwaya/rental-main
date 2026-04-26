package com.xpro.rentalmain.rentalmain.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic in-memory cache that behaves like your redisWeightCache.
 * Replace internals later with RedisTemplate / Lettuce if needed.
 */
@Component
public class RedisWeightCache<K, V> {

    private final Map<K, CacheEntry<V>> store = new ConcurrentHashMap<>();

    // =========================
    // BASIC PUT
    // =========================

    public void put(K key, V value) {
        store.put(key, new CacheEntry<>(value, 0));
    }

    public void put(K key, V value, Duration ttl) {
        long expiresAt = System.currentTimeMillis() + ttl.toMillis();
        store.put(key, new CacheEntry<>(value, expiresAt));
    }

    public void putAll(Map<K, V> values) {
        values.forEach(this::put);
    }

    public void putAll(Map<K, V> values, Duration ttl) {
        values.forEach((k, v) -> put(k, v, ttl));
    }

    // =========================
    // GET
    // =========================

    public V get(K key) {
        CacheEntry<V> entry = store.get(key);

        if (entry == null) return null;

        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }

        return entry.value;
    }

    public Map<K, V> getAll(Collection<K> keys) {
        Map<K, V> result = new ConcurrentHashMap<>();

        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }

        return result;
    }

    // =========================
    // REMOVE
    // =========================

    public void remove(K key) {
        store.remove(key);
    }

    public void removeAll(Collection<K> keys) {
        keys.forEach(store::remove);
    }

    public void clear() {
        store.clear();
    }

    // =========================
    // CHECKS
    // =========================

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        cleanupExpired();
        return store.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Set<K> keys() {
        cleanupExpired();
        return store.keySet();
    }

    // =========================
    // INTERNAL CLEANUP
    // =========================

    private void cleanupExpired() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    // =========================
    // ENTRY WRAPPER
    // =========================

    private static class CacheEntry<V> {
        private final V value;
        private final long expiresAt; // 0 means no expiry

        CacheEntry(V value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
        }
    }
}