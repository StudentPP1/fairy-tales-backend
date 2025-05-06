package dev.project.bedtimestory.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheEvictScheduler {
    private final CacheManager cacheManager;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void evictCaches() {
        clearCache("notReadStories");
        clearCache("searchStories");
        clearCache("mostLikedStories");
    }
    private void clearCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        log.info("Cleared cache: {}", cacheName);
    }
}