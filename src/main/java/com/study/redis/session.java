package com.study.redis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class session {

//    private static final Duration SESSION_EXPIRE = Duration.ofHours(1);   // 세션 만료 시간: 1시간
    private static final Duration SESSION_EXPIRE = Duration.ofSeconds(10);   // 세션 만료 시간: 1시간
    private static final String COOKIE_NAME = "session_id";

    private final StringRedisTemplate redisTemplate;

    public record LoginRequest(String userId) {
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        // 1. 고유하고 추측 불가능한 세션 ID 생성
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = "session:" + sessionId;

        // 2. 유저 정보를 Hash로 저장
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("user_id", loginRequest.userId());
        userInfo.put("tier", "Premium");
        userInfo.put("ip", request.getRemoteAddr());
        redisTemplate.opsForHash().putAll(sessionKey, userInfo);

        // 3. 세션 만료 시간 설정 (1시간)
        redisTemplate.expire(sessionKey, SESSION_EXPIRE);

        // 4. 쿠키로 세션 ID 전달 (HttpOnly, Secure, SameSite)
        //    로컬(HTTP) 테스트에서는 secure(false). 운영(HTTPS)에서는 반드시 true
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, sessionId)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of("message", "Login Success", "session_id", sessionId);

    }


    @GetMapping("/me")
    public Map<?, ?> getMyInfo(@CookieValue(name = COOKIE_NAME, required = false) String sessionId) {
        if (sessionId == null) {
            return Map.of("error", "Not logged in");
        }
        String sessionKey = "session:" + sessionId;

        // 1. 레디스에서 세션 정보 조회
        Map<Object, Object> userInfo = redisTemplate.opsForHash().entries(sessionKey);
        if (userInfo.isEmpty()) {
            return Map.of("error", "Session expired or invalid");
        }

        // 2. 활동 중이므로 세션 시간 연장 (Sliding Window)
        redisTemplate.expire(sessionKey, SESSION_EXPIRE);

        return userInfo;
    }



}
