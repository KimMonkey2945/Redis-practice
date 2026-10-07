package com.study.redis.user.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.redis.user.dto.UserProfile;
import com.study.redis.user.dto.UserProfileDto;
import com.study.redis.user.dto.UserProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileJpaService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(300);

    private final StringRedisTemplate redisTemplate;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) throws JsonProcessingException {
        String cacheKey = "user:profile:" + userId;

        // 1. 레디스에서 캐시 확인
        String cachedUser = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.info("🚀 Cache Hit! (userId: {})", userId);
            // JSON 문자열 → 객체
            return objectMapper.readValue(cachedUser, UserProfileDto.class);
        }

        // 2. 캐시 Miss 시 실제 DB 조회
        log.info("🐌 Cache Miss! Fetching from DB... (userId: {})", userId);
        UserProfile user = userProfileRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // DB 조회가 느리다고 가정 (2초 대기 시뮬레이션)
        sleep(2000);

        // 3. Redis에 저장 (TTL 300초) - 객체 → JSON 문자열
        UserProfileDto dto = new UserProfileDto(user.getUserId(), user.getName(), user.getEmail(), user.getTier());
        redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dto), CACHE_TTL);

        return dto;
    }

    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        String cacheKey = "user:profile:" + userId;

        // 1. DB 데이터 수정 (변경 감지로 커밋 시 UPDATE)
        UserProfile user = userProfileRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.changeProfile(request.name(), request.email(), request.tier());

        // 2. [핵심] 캐시 삭제 (Cache Invalidation) - DB 커밋이 끝난 뒤에 실행되도록 등록
        evictAfterCommit(cacheKey);

        return new UserProfileDto(user.getUserId(), user.getName(), user.getEmail(), user.getTier());
    }

    private void evictAfterCommit(String cacheKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.delete(cacheKey);
            }
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}