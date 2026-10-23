package com.study.redis.prac.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.study.redis.prac.post.QPost.post;

@RequiredArgsConstructor
public class PracPostRepositoryImpl implements PracPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public long addViewCount(Long postId, long count) {
//        throw new RuntimeException("테스트용 강제 실패");
        return queryFactory.update(post)
                .set(post.viewCount, post.viewCount.add(count))
                .where(post.id.eq(postId))
                .execute();
    }
}
