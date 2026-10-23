package com.study.redis.prac.common;

public final class RedisKeys {

    private static final String PREFIX = "prac";

    // 스케줄러에서 SCAN할 때 쓰는 패턴
    public static final String POST_VIEWS_PATTERN = PREFIX + ":post:views:*";

    private RedisKeys() {
    }

    // prac:product:{productId}
    public static String product(Long productId) {
        return PREFIX + ":product:" + productId;
    }

    // prac:post:views:{postId}  → 아직 DB에 반영 안 된 조회수
    public static String postViews(Long postId) {
        return PREFIX + ":post:views:" + postId;
    }

    // "prac:post:views:3" → 3
    public static Long extractPostId(String postViewsKey) {
        return Long.parseLong(postViewsKey.substring(postViewsKey.lastIndexOf(':') + 1));
    }


}
