package com.study.redis.prac.post;

public record PostResponse(

        Long id,
        String title,
        String content,
        long viewCount

) {

    public static PostResponse of(Post post, long pendingViewCount) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getViewCount() + pendingViewCount);
    }


}
