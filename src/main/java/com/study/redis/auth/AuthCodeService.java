package com.study.redis.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCodeService {

    // 인증번호 유효 시간 (300초 = 5분)
    public static final Duration AUTH_TIMEOUT = Duration.ofSeconds(60);

    // 예측하기 어려운 난수 생성기 (보안 용도에는 Random 대신 SecureRandom)
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;

    private static final RedisScript<Long> VERIFY_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/verify_code.lua"), Long.class);

    public String sendCode(String phone){
        // 1. 6자리 난수 생성 (100000 ~ 999999)
        String code = String.valueOf(100000 + random.nextInt(900000));

        // [보안] 전화번호 같은 개인정보는 그대로 키로 쓰지 않고 해싱해서 저장
        String cacheKey = "auth:code:" + sha256(phone);

        // 2. Redis에 저장 (TTL 300초)
        redisTemplate.opsForValue().set(cacheKey, code, AUTH_TIMEOUT);

        // 3. 실제로는 여기서 SMS 발송
        log.info("📧 [SMS 발송] To: {}, Code: {}", phone, code);
        return code;

    }

//    public void verifyCode(String phone, String inputCode) {
//        String cacheKey = "auth:code:" + sha256(phone);
//
//        // 1. Redis에서 저장된 번호 조회
//        String savedCode = redisTemplate.opsForValue().get(cacheKey);
//
//        // 2. 값이 없으면 만료되었거나 요청한 적이 없는 것
//        if (savedCode == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired or not requested");
//        }
//
//        // 3. 입력값 비교
//        if (!savedCode.equals(inputCode)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
//        }
//
//        // 4. [핵심] 인증 성공 시 보안을 위해 즉시 삭제
//        redisTemplate.delete(cacheKey);
//    }

    public void verifyCode(String phone, String inputCode) {
        String cacheKey = "auth:code:" + sha256(phone);

        // KEYS[1] = cacheKey, ARGV[1] = inputCode
        Long result = redisTemplate.execute(VERIFY_SCRIPT, List.of(cacheKey), inputCode);

        if (result == -1L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired or not requested");
        }
        if (result == 0L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }
        // result == 1 : 일치했고 이미 삭제됨
    }


    /** hashlib.sha256(phone.encode()).hexdigest() 와 같은 결과 */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
