package com.study.redis.article.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Article a SET a.viewCount = :viewCount WHERE a.articleId = :articleId")
    int updateViewCount(@Param("articleId") Long articleId, @Param("viewCount") long viewCount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Article a SET a.likeCount = :likeCount WHERE a.articleId = :articleId")
    int updateLikeCount(@Param("articleId") Long articleId, @Param("likeCount") long likeCount);


}
