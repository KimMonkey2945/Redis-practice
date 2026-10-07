package com.study.redis.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.redis.user.dto.UserProfileDto;
import com.study.redis.user.dto.UserProfileUpdateRequest;
import com.study.redis.user.jpa.UserProfileJpaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileJpaService jpaService;
//    private final UserProfileMybatisService mybatisService;

    // ---------- [A] JPA ----------
    @GetMapping("/jpa/users/{userId}")
    public UserProfileDto getUserJpa(@PathVariable Long userId) throws JsonProcessingException {
        return jpaService.getUserProfile(userId);
    }

    @PutMapping("/jpa/users/{userId}")
    public Map<String, Object> updateUserJpa(@PathVariable Long userId, @RequestBody UserProfileUpdateRequest request) {
        UserProfileDto current = jpaService.updateUserProfile(userId, request);
        return Map.of("message", "updated successfully", "current_data", current);
    }

//    // ---------- [B] MyBatis ----------
//    @GetMapping("/mybatis/users/{userId}")
//    public UserProfileDto getUserMybatis(@PathVariable Long userId) throws JsonProcessingException {
//        return mybatisService.getUserProfile(userId);
//    }
//
//    @PutMapping("/mybatis/users/{userId}")
//    public Map<String, Object> updateUserMybatis(@PathVariable Long userId, @RequestBody UserProfileUpdateRequest request) {
//        UserProfileDto current = mybatisService.updateUserProfile(userId, request);
//        return Map.of("message", "updated successfully", "current_data", current);
//    }
}