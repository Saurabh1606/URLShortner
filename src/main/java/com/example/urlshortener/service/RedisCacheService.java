package com.example.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private static final String URL_MAPPING_PREFIX = "url:";
    private static final String REVERSE_MAPPING_PREFIX = "reverse:";
    private static final long DEFAULT_TTL = 30; // 30 days


    public void cacheUrlMapping(String shortCode, String longUrl) {
        String key = URL_MAPPING_PREFIX + shortCode;
        redisTemplate.opsForValue().set(key, longUrl, DEFAULT_TTL, TimeUnit.DAYS);
    }


    public void cacheReverseMapping(String longUrl, String shortCode) {
        String key = REVERSE_MAPPING_PREFIX + longUrl.hashCode();
        redisTemplate.opsForValue().set(key, shortCode, DEFAULT_TTL, TimeUnit.DAYS);
    }


    public String getOriginalUrl(String shortCode) {
        String key = URL_MAPPING_PREFIX + shortCode;
        return (String) redisTemplate.opsForValue().get(key);
    }


    public String getExistingShortCode(String longUrl) {
        String key = REVERSE_MAPPING_PREFIX + longUrl.hashCode();
        return (String) redisTemplate.opsForValue().get(key);
    }


    public boolean existsShortCode(String shortCode) {
        String key = URL_MAPPING_PREFIX + shortCode;
        return redisTemplate.hasKey(key);
    }


    public void deleteMapping(String shortCode) {
        String key = URL_MAPPING_PREFIX + shortCode;
        redisTemplate.delete(key);
    }


    public void setCacheTTL(String shortCode, long ttl, TimeUnit timeUnit) {
        String key = URL_MAPPING_PREFIX + shortCode;
        redisTemplate.expire(key, ttl, timeUnit);
    }
}
