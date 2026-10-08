package com.study.redis.article;

public interface ArticleCountSync {

    /** Redis의 조회수 값을 DB에 반영 */
    void syncViewCount(Long articleId, long viewCount);

    /** Redis의 좋아요 수를 DB에 반영 */
    void syncLikeCount(Long articleId, long likeCount);

}
