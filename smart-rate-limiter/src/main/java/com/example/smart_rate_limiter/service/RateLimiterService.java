package com.example.smart_rate_limiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${ratelimiter.max-tokens}")
    private double MAX_TOKENS;

    @Value("${ratelimiter.refill-rate}")
    private double REFILL_RATE;

    public boolean AllowedRequest(String clientId) {
        String tokensKey = "ratelimit:" + clientId + ":tokens";
        String timeKey = "ratelimit:" + clientId + ":lastRefill";

        double tokens = getDouble(tokensKey, MAX_TOKENS);
        long lastRefill = getLong(timeKey, System.currentTimeMillis());

        long now = System.currentTimeMillis();
        double secondsPassed = (now - lastRefill) / 1000.0;
        tokens = Math.min(MAX_TOKENS, tokens + secondsPassed * REFILL_RATE);

        boolean allowed;
        if (tokens >= 1.0) {
            tokens -= 1.0;
            allowed = true;
            logger.info("ALLOWED request for client [{}] - tokens remaining: {}", clientId, String.format("%.2f", tokens));
        } else {
            allowed = false;
            logger.warn("BLOCKED request for client [{}] - rate limit exceeded, tokens: {}", clientId, String.format("%.2f", tokens));
        }

        redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens));
        redisTemplate.opsForValue().set(timeKey, String.valueOf(now));

        return allowed;
    }

    public Map<String, Object> getStats(String clientId) {
        String tokensKey = "ratelimit:" + clientId + ":tokens";
        String timeKey = "ratelimit:" + clientId + ":lastRefill";

        double tokens = getDouble(tokensKey, MAX_TOKENS);
        long lastRefill = getLong(timeKey, System.currentTimeMillis());

        long now = System.currentTimeMillis();
        double secondsPassed = (now - lastRefill) / 1000.0;
        tokens = Math.min(MAX_TOKENS, tokens + secondsPassed * REFILL_RATE);

        redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens));
        redisTemplate.opsForValue().set(timeKey, String.valueOf(now));

        // "count" ab tokens consumed dikhata hai (max - remaining), jo naturally refill ke saath ghatta hai
        int usedTokens = (int) Math.round(MAX_TOKENS - tokens);

        Map<String, Object> result = new HashMap<>();
        result.put("clientId", clientId);
        result.put("count", usedTokens);
        result.put("limit", (int) MAX_TOKENS);
        result.put("avgRate", Math.round(tokens * 100.0) / 100.0);
        return result;
    }

    private double getDouble(String key, double defaultVal) {
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Double.parseDouble(val) : defaultVal;
    }

    private long getLong(String key, long defaultVal) {
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : defaultVal;
    }
}
