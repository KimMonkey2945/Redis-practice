package com.study.redis.user.jpa;

import com.study.redis.user.dto.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
