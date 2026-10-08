package com.study.redis.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthCodeController {

    private final AuthCodeService authCodeService;

    public record SendCodeRequest(String phone) {
    }

    public record VerifyCodeRequest(String phone, String inputCode) {
    }

    @PostMapping("/auth/send")
    public Map<String, Object> sendVerificationCode(@RequestBody SendCodeRequest request) {
        String code = authCodeService.sendCode(request.phone());
        // 실습 확인용으로 code를 응답에 포함 (실제 서비스에서는 절대 내려주지 않음)
        return Map.of("message", "Verification code sent", "code", code,
                "expires_in", AuthCodeService.AUTH_TIMEOUT.toSeconds());
    }

    @PostMapping("/auth/verify")
    public Map<String, Object> verifyCode(@RequestBody VerifyCodeRequest request) {
        authCodeService.verifyCode(request.phone(), request.inputCode());
        return Map.of("message", "Authentication successful");
    }


}
