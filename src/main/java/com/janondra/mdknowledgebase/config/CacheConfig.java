package com.janondra.mdknowledgebase.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new SimpleCacheManager();

        var userIdCache = new CaffeineCache(
            "userIdCache",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(60))
                .build()
        );

        cacheManager.setCaches(List.of(userIdCache));

        return cacheManager;
    }

    @Bean
    public Cache userIdCache(CacheManager cacheManager) {
        return cacheManager.getCache("userIdCache");
    }

}
