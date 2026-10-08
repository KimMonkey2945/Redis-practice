package com.study.redis.stock;

import com.study.redis.stock.jpa.StockJpaService;
//import com.study.redis.stock.mybatis.StockMybatisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class StockLockFacade {

    private static final Duration ACQUIRE_TIMEOUT = Duration.ofSeconds(10);   // 락 대기 최대 시간
    private static final Duration LOCK_TIMEOUT = Duration.ofMillis(5000);     // 락 유효 시간

    private final RedisLockManager redisLockManager;
    private final StockJpaService stockJpaService;
//    private final StockMybatisService stockMybatisService;

    public int decreaseWithJpa(Long itemId, String userId) {
        return executeWithLock(itemId, userId, () -> stockJpaService.decreaseStock(itemId, userId));
    }

//    public int decreaseWithMybatis(Long itemId, String userId) {
//        return executeWithLock(itemId, userId, () -> stockMybatisService.decreaseStock(itemId, userId));
//    }

    private int executeWithLock(Long itemId, String userId, Supplier<Integer> task) {
        String lockName = "lock:item:" + itemId;

        // 1. 락 획득 시도
        String lockId = redisLockManager.acquireLock(lockName, ACQUIRE_TIMEOUT, LOCK_TIMEOUT);
        if (lockId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "현재 접속자가 많아 처리가 지연되고 있습니다. 다시 시도해주세요. (요청자: " + userId + ")");
        }

        try {
            // 2. 임계 영역: 트랜잭션 시작 ~ 커밋이 이 호출 안에서 끝난다
            return task.get();
        } finally {
            // 3. 예외가 나도 반드시 해제 (커밋이 끝난 뒤)
            redisLockManager.releaseLock(lockName, lockId);
        }
    }
}
