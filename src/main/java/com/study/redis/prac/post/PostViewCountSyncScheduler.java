package com.study.redis.prac.post;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.study.redis.prac.common.RedisKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewCountSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final PostViewCountService postViewCountService;
    private final PracPostRepository postRepository;

    /**
     * 이전 실행이 끝나고 10초 뒤 다시 실행 (테스트용. 실무는 보통 1~5분)
     */
    @Scheduled(fixedDelay = 10_000)
    public void syncViewCounts() {
        List<String> keys = scanKeys(RedisKeys.POST_VIEWS_PATTERN);
        if (keys.isEmpty()) {
            return;
        }

        log.info("[조회수 동기화 시작] 대상 키 {}개", keys.size());

        for (String key : keys) {
            Long postId = RedisKeys.extractPostId(key);

            // 1. Redis에서 꺼내면서 삭제
            long count = postViewCountService.getAndReset(postId);
            if (count == 0) {
                continue;
            }

            // 2. DB에 더하기 (Querydsl)
            try {
                postRepository.addViewCount(postId, count);
                log.info("[조회수 동기화] postId={} +{}", postId, count);
            } catch (Exception e) {
                // 3. 실패하면 Redis에 되돌려서 다음 주기에 재시도
                postViewCountService.restore(postId, count);
                log.error("[조회수 동기화 실패 → Redis 복구] postId={} count={}", postId, count, e);
            }
        }
    }

    /**
     * KEYS 대신 SCAN: 나눠서 찾기 때문에 Redis를 오래 붙잡지 않음
     */
    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }
}