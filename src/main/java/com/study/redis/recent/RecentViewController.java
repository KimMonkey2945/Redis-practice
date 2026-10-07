package com.study.redis.recent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecentViewController {


    private static final int MAX_SIZE = 5;
    private final StringRedisTemplate redisTemplate;



    /** 최근 본 상품 업데이트 */
    @PostMapping("/products/{productId}/view")
    public Map<String, Object> viewProduct(@PathVariable String productId,
                                           @RequestParam(defaultValue = "user1") String userId) {

        ListOperations<String, String> list = redisTemplate.opsForList();
        String key = "user:" + userId + ":recent_views";

        // 1. 기존 리스트에 같은 ID가 있으면 제거 (중복 방지 + 끌어올리기)
        //    LREM key count value : count가 0이면 일치하는 항목을 모두 삭제
        list.remove(key, 0, productId);

        // 2. 최신 상품 ID를 왼쪽(맨 앞)에 추가
        list.leftPush(key, productId);

        // 3. 최신 5개만 남기고 자르기 (인덱스 0~4 = 5개)
        list.trim(key, 0, MAX_SIZE - 1);

        return Map.of("message", "Product " + productId + " added to recent views");

    }

    @GetMapping("/users/{userId}/recent-views")
    public Map<String, Object> getRecentViews(@PathVariable String userId) {

        String key = "user:" + userId + ":recent_views";
        List<String> views = redisTemplate.opsForList().range(key, 0, -1);

        return Map.of("recent_views", views == null ? List.of() : views);
    }



}
