package com.rushtix.core.feature.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate redisTemplate;
    private static final String LOCK_KEY_PREFIX = "seat:lock:";

    public boolean acquireSeatLocks(List<UUID> seatIds,UUID userId,int ttlMinutes)
    {
        List<String> successfullyLocks=new java.util.ArrayList<>();
        for (UUID seatId : seatIds) {
            String lockKey = LOCK_KEY_PREFIX + seatId;
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(), java.time.Duration.ofMinutes(ttlMinutes));
            if (Boolean.TRUE.equals(success)) {
                successfullyLocks.add(lockKey);
            } else {
                // Failed to acquire lock, release any previously acquired locks
                redisTemplate.delete(successfullyLocks);
                return false;
            }
        }
        return true;
    }

    public void releaseSeatLocks(List<UUID> seatIds,UUID userId)
    {
        String userIdStr = userId.toString();
        List<String> keysToDelete = new java.util.ArrayList<>();

        for (UUID seatId : seatIds) {
            String lockKey = LOCK_KEY_PREFIX + seatId;
            String currentValue = redisTemplate.opsForValue().get(lockKey);

            if (userIdStr.equals(currentValue)) {
                keysToDelete.add(lockKey);
            }
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }
}
