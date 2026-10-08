package com.study.redis.article.jpa;

import com.study.redis.article.ArticleCountSync;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("jpaArticleCountSync")
@RequiredArgsConstructor
public class JpaArticleCountSync implements ArticleCountSync {

    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void syncViewCount(Long articleId, long viewCount) {
        int updatedCount = articleRepository.updateViewCount(articleId, viewCount);
        log.info("📢 [JPA Backup] article {} views = {} (updated rows: {})", articleId, viewCount, updatedCount);
    }

    @Override
    @Transactional
    public void syncLikeCount(Long articleId, long likeCount) {
        int updatedCount = articleRepository.updateLikeCount(articleId, likeCount);
        log.info("📢 [JPA Backup] article {} likes = {} (updated rows: {})", articleId, likeCount, updatedCount);
    }
}
