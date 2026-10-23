package com.study.redis.prac.post;

import com.study.redis.prac.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewCountService {

    private final StringRedisTemplate redisTemplate;

    /**
     * INCR prac:post:views:{postId}
     * 키가 없으면 0에서 시작해서 1 → 초기화 필요 없음
     */
    public long increase(Long postId){
        Long count = redisTemplate.opsForValue().increment(RedisKeys.postViews(postId));
        return count == null ? 0 : count;
    }

    /**
     * GETDEL prac:post:views:{postId}
     * 꺼내면서 삭제. 그 뒤 들어온 조회는 새 키에 1부터 다시 쌓임
     */
    public long getAndReset(Long postId){
        String value = redisTemplate.opsForValue().getAndDelete(RedisKeys.postViews(postId));
        return value == null ? 0 : Long.parseLong(value);
    }

    /**
     * INCRBY prac:post:views:{postId} {count}
     * DB 반영 실패 시 되돌려 놓기
     */
    public void restore(Long postId, long count){
        redisTemplate.opsForValue().increment(RedisKeys.postViews(postId), count);
    }


}
