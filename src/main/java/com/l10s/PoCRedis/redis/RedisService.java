package com.l10s.PoCRedis.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveReservationWithTTL(String reservationId, Object reservationData, Duration ttl) {
        String key = "reservation:" + reservationId;
        redisTemplate.opsForValue().set(key, reservationData, ttl);
    }

    public boolean existsReservation(String reservationId) {
        String key = "reservation:" + reservationId;
        return redisTemplate.hasKey(key);
    }

    public boolean deleteReservation(String reservationId) {
        String key = "reservation:" + reservationId;
        return redisTemplate.delete(key);
    }

    public long getReservationTTL(String reservationId) {
        String key = "reservation:" + reservationId;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

}
