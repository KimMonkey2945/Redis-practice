package com.study.redis.prac.post;

public interface PracPostRepositoryCustom {

    long addViewCount(Long postId, long count);

}
