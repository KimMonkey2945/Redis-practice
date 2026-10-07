package com.study.redis.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 캐시(Redis)에 JSON으로 저장되고, API 응답으로 나가는 객체
 * - Jackson 역직렬화, MyBatis 결과 매핑을 위해 기본 생성자 + setter 필요
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long userId;
    private String name;
    private String email;
    private String tier;
}