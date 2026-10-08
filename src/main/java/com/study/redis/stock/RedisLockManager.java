package com.study.redis.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private static final RedisScript<Long> RELEASE_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/release_lock.lua"), Long.class);

    private final StringRedisTemplate redisTemplate;


    /**
     * @param acquireTimeout 락을 얻기 위해 기다릴 최대 시간 (무한정 기다리지 않음)
     * @param lockTimeout    락 자체의 유효 시간
     * @return 성공 시 내 토큰, 실패 시 null
     */
    public String acquireLock(String lockName, Duration acquireTimeout, Duration lockTimeout) {
        String identifier = UUID.randomUUID().toString();   // 내가 잡은 락임을 증명하는 고유 토큰
        long endTime = System.currentTimeMillis() + acquireTimeout.toMillis();

        while (System.currentTimeMillis() < endTime) {
            // NX: 키가 없을 때만 생성, PX: 밀리초 TTL
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockName, identifier, lockTimeout);
            if (Boolean.TRUE.equals(acquired)) {
                return identifier;
            }
            // 실패하면 0.1초 쉬고 재시도 (Spin Lock)
            sleep(100);
        }
        return null;
    }

    public boolean releaseLock(String lockName, String identifier) {
        Long result = redisTemplate.execute(RELEASE_SCRIPT, List.of(lockName), identifier);
        return result != null && result == 1L;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}
