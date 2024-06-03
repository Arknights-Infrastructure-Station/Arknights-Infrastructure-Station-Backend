package com.arknightsinfrastructurestationbackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * CacheConfig class is responsible for configuring the caching mechanism
 * using Caffeine cache within a Spring Boot application. The cache
 * specifications are loaded from the application's configuration properties.
 */
@Configuration
@ConfigurationProperties(prefix = "caching")
@Data
@Slf4j
public class CacheConfig {

    // A map to hold cache specifications, where the key is the cache name and the value is the cache specification.
    private Map<String, CacheSpec> specs;

    /**
     * Bean definition for CacheManager.
     *
     * @param ticker The Ticker instance to use for controlling cache timing.
     * @return A configured CacheManager instance.
     */
    @Bean
    public CacheManager cacheManager(Ticker ticker) {
        SimpleCacheManager manager = new SimpleCacheManager();

        // Check if cache specifications are provided
        if (specs != null) {
            // Create a list of CaffeineCache instances based on the provided specifications
            List<CaffeineCache> caches = specs.entrySet().stream()
                    .map(entry -> buildCache(entry.getKey(), entry.getValue(), ticker))
                    .collect(Collectors.toList());
            manager.setCaches(caches);
        }

        return manager;
    }

    /**
     * Builds a CaffeineCache instance based on the provided cache name and specifications.
     *
     * @param name The name of the cache.
     * @param cacheSpec The specifications of the cache including timeout and max size.
     * @param ticker The Ticker instance to use for controlling cache timing.
     * @return A configured CaffeineCache instance.
     */
    private CaffeineCache buildCache(String name, CacheSpec cacheSpec, Ticker ticker) {
        // Log the cache configuration details
        log.info("Cache {} specified timeout of {} min, max of {}", name, cacheSpec.getTimeout(), cacheSpec.getMax());

        // Build the Caffeine cache instance with the specified timeout and maximum size
        final Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(cacheSpec.getTimeout(), TimeUnit.MINUTES)
                .maximumSize(cacheSpec.getMax())
                .ticker(ticker);

        return new CaffeineCache(name, caffeineBuilder.build());
    }

    /**
     * Bean definition for Ticker.
     *
     * @return A system Ticker instance.
     */
    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }

    /**
     * CacheSpec class represents the specifications for a cache.
     * It includes timeout and maximum size attributes.
     */
    @Data
    public static class CacheSpec {
        private Integer timeout; // Timeout duration for cache entries in minutes
        private Integer max = 200; // Maximum size of the cache with a default value of 200
    }
}