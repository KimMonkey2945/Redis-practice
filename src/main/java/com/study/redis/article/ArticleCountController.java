package com.study.redis.article;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class ArticleCountController {

    private static final long BACKUP_INTERVAL = 100;   // 100회마다 DB 반영

    private final StringRedisTemplate redisTemplate;
    private final ArticleCountSync articleCountSync;

    // @Qualifier 때문에 Lombok 대신 생성자를 직접 작성
    public ArticleCountController(StringRedisTemplate redisTemplate,
                                  @Qualifier("jpaArticleCountSync") ArticleCountSync articleCountSync) {
        this.redisTemplate = redisTemplate;
        this.articleCountSync = articleCountSync;
    }

    /** 조회수 증가 */
    @PostMapping("/articles/{articleId}/view")
    public Map<String, Object> increaseViewCount(@PathVariable Long articleId) {

        String viewKey = "article:" + articleId + ":views";
        // 1. 원자적으로 값 증가 (INCR)
        //    GET으로 읽어서 더할 필요가 없음 → Race Condition 차단
        Long currentViews = redisTemplate.opsForValue().increment(viewKey);
        if (currentViews == null) {
            currentViews = 0L;
        }

        // 2. (선택) 100 단위로 오를 때만 실제 DB에 동기화
        if (currentViews % BACKUP_INTERVAL == 0) {
            log.info("📢 [Backup] Article {} reached {} views. Syncing to DB...", articleId, currentViews);
            articleCountSync.syncViewCount(articleId, currentViews);
        }
        return Map.of("article_id", articleId, "total_views", currentViews);

    }

    /** 조회수 + 좋아요 수 조회 */
    @GetMapping("/articles/{articleId}/stats")
    public Map<String, Object> getArticleStats(@PathVariable Long articleId) {
        String viewKey = "article:" + articleId + ":views";
        String likeKey = "article:" + articleId + ":likes";

        // GET은 문자열을 돌려주고, 키가 없으면 null
        String views = redisTemplate.opsForValue().get(viewKey);
        String likes = redisTemplate.opsForValue().get(likeKey);

        return Map.of(
                "article_id", articleId,
                "views", toLong(views),
                "likes", toLong(likes)
        );
    }

    /** 문자열 카운터 → 숫자 (없으면 0) */
    private long toLong(String value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value);
    }



}
