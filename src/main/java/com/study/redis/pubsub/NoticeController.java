package com.study.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    // 공지사항용 채널명
    private static final String NOTICE_CHANNEL = "system:notices";
    private static final ChannelTopic NOTICE_TOPIC = new ChannelTopic(NOTICE_CHANNEL);

    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    /** [발행자] Swagger에서 공지를 발행합니다. */
    @PostMapping("/publish-notice")
    public Map<String, Object> sendNotice(@RequestParam String message) {
        // 채널에 메시지 전송 (받은 구독자 수를 반환)
        Long subscriberCount = redisTemplate.convertAndSend(NOTICE_CHANNEL, message);
        return Map.of("status", "success", "received_subscribers", subscriberCount == null ? 0 : subscriberCount);
    }

    /** [수신자] SSE로 실시간 알림을 받습니다. */
    @GetMapping(value = "/stream-notices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotices() {
        // 브라우저 1명과의 SSE 연결 (30분 뒤 만료)
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());

        // [핵심] 이 브라우저 전용 리스너를 만들어 구독 컨테이너에 등록
        MessageListener listener = (message, pattern) -> {
            String data = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                // SSE 표준 포맷("data: 내용\n\n")으로 전송
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                emitter.completeWithError(e);   // 브라우저가 이미 닫힘
            }
        };
        listenerContainer.addMessageListener(listener, NOTICE_TOPIC);

        // 브라우저가 창을 닫거나, 시간이 다 되거나, 에러가 나면 리스너 해제 (구독 해제)
        Runnable cleanup = () -> listenerContainer.removeMessageListener(listener, NOTICE_TOPIC);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }
}