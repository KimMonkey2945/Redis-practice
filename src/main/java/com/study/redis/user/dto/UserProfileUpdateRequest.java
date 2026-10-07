package com.study.redis.user.dto;

/** 수정 요청 Body (Pydantic UserProfileUpdate 역할) */
public record UserProfileUpdateRequest(String name, String email, String tier) {
}