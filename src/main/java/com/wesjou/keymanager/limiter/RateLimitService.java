package com.wesjou.keymanager.limiter;

import com.wesjou.keymanager.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Bucket> concurrentHashMap = new ConcurrentHashMap<>();

    public void enforceRateLimit(String key, RateLimitPolicy policy) {
        var cacheKey = policy.name() + ":" + key;

        var bucket = concurrentHashMap.computeIfAbsent(cacheKey, k -> createNewBucket(policy));

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }
    }

    private Bucket createNewBucket (RateLimitPolicy policy) {
        return switch (policy) {
            case LOGIN -> loginBucket();
            case REGISTER -> registerBucket();
            case AUTH -> authBucket();
            case NONE -> throw new IllegalArgumentException();
        };
    }

    private Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                .build();
    }

    private Bucket registerBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(3).refillGreedy(3, Duration.ofMinutes(1)))
                .build();
    }

    private Bucket authBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(60).refillGreedy(60, Duration.ofMinutes(1)))
                .build();
    }

}
