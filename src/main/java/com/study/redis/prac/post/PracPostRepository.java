package com.study.redis.prac.post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PracPostRepository extends JpaRepository<Post, Long>, PracPostRepositoryCustom {



}
