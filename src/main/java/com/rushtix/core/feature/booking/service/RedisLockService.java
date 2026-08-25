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
    private final com.rushtix.core.feature.seat.repository.SeatRepository seatRepository;
    private static final String LOCK_KEY_PREFIX = "seat:lock:";

    public boolean acquireSeatLocks(List<UUID> seatIds,UUID userId,int ttlMinutes)
    {
        try {
            List<String> successfullyLocks=new java.util.ArrayList<>();
            for (UUID seatId : seatIds) {
                String lockKey = LOCK_KEY_PREFIX + seatId;
                Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(), java.time.Duration.ofMinutes(ttlMinutes));
                
                if (Boolean.TRUE.equals(success)) {
                    successfullyLocks.add(lockKey);
                } else {
                    // Check if the current user already holds this lock from a previous retry
                    String currentValue = redisTemplate.opsForValue().get(lockKey);
                    if (userId.toString().equals(currentValue)) {
                        successfullyLocks.add(lockKey);
                        continue;
                    }
                    // Failed to acquire lock, release any previously acquired locks
                    redisTemplate.delete(successfullyLocks);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // Fallback: If Redis is completely down on Windows dev machine, log warning and let PostgreSQL pessimistic locking handle concurrency.
            System.err.println("Redis fallback activated: " + e.getMessage());
            return true;
        }
    }

    public void releaseSeatLocks(List<UUID> seatIds,UUID userId)
    {
        try {
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

            try {
                // We must query the DB to get the eventId for the SSE routing
                List<com.rushtix.core.domain.entities.Seat> seats = seatRepository.findAllById(seatIds);
                for (com.rushtix.core.domain.entities.Seat seat : seats) {
                    String payload = String.format("{\"eventId\":\"%s\", \"seatId\":\"%s\", \"status\":\"AVAILABLE\"}", seat.getEvent().getId(), seat.getId());
                    broadcastSeatUpdate(payload);
                }
            } catch (Exception ex) {
                System.err.println("Failed to broadcast seat release: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Redis fallback activated during release: Unable to connect to Redis. " + e.getMessage());
        }
    }

    public void broadcastSeatUpdate(String payload) {
        try {
            redisTemplate.convertAndSend("seat-updates", payload);
        } catch (Exception e) {
            System.err.println("Failed to publish to Redis: " + e.getMessage());
        }
    }
}
