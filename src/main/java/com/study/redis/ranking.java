package com.study.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ranking {

    private static final String RANKING_KEY = "leaderboard:daily:2026-03-13";

    private final StringRedisTemplate redisTemplate;

    /** 유저의 점수를 실시간으로 업데이트합니다. */
    @PostMapping("/rank/score")
    public Map<String, Object> updateScore(@RequestParam String userId, @RequestParam double scoreDelta) {
        // ZINCRBY: 기존 점수에 합산 (키나 멤버가 없으면 자동 생성)
        Double newScore = redisTemplate.opsForZSet().incrementScore(RANKING_KEY, userId, scoreDelta);

        return Map.of("user_id", userId, "current_score", newScore);
    }

    /** 상위 10명을 조회합니다. */
    @GetMapping("/rank/top10")
    public Map<String, Object> getTopRankers() {
        // 점수 높은 순으로 0~9번 (점수 포함)
        Set<ZSetOperations.TypedTuple<String>> topList = redisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, 9);

        return Map.of("top_rankers", toRankList(topList, 0));
    }

    /** 특정 유저를 기준으로 앞뒤 2명씩, '내 주변 5명'을 조회합니다. */
    @GetMapping("/rank/around-me/{userId}")
    public Map<String, Object> getNearbyRank(@PathVariable String userId) {
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

        // 1. 나의 현재 등수 (0부터 시작)
        Long myRank = zSet.reverseRank(RANKING_KEY, userId);
        if (myRank == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ranking data not found");
        }

        // 2. 내 주변 범위 계산 (내 앞 2명 ~ 내 뒤 2명)
        long start = Math.max(0, myRank - 2);
        long end = myRank + 2;

        // 3. 해당 범위 유저들 조회
        Set<TypedTuple<String>> nearbyList = zSet.reverseRangeWithScores(RANKING_KEY, start, end);

        return Map.of("user_id", userId, "nearby_rankers", toRankList(nearbyList, start));
    }

    /** 조회 결과를 [{rank, user_id, score}, ...] 형태로 변환 */
    private List<Map<String, Object>> toRankList(Set<TypedTuple<String>> tuples, long startIndex) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (tuples == null) {
            return result;
        }

        long rank = startIndex + 1;   // 화면에 보여줄 등수는 1부터
        for (TypedTuple<String> tuple : tuples) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank);
            row.put("user_id", tuple.getValue());
            row.put("score", tuple.getScore());
            result.add(row);
            rank++;
        }
        return result;
    }
}
