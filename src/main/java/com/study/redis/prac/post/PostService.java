package com.study.redis.prac.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PracPostRepository postRepository;
    private final PostViewCountService postViewCountService;

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(() ->  new IllegalArgumentException("게시글이 없습니다. id=" + postId));
        long pendingViewCount = postViewCountService.increase(postId);
        return PostResponse.of(post, pendingViewCount);

    }

}
