package com.study.redis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConnectionChecker {

    private final RedisConnectionFactory connectionFactory;

    @EventListener(ApplicationReadyEvent.class)
    public void checkConnection() {
        String pong = connectionFactory.getConnection().ping();
        log.info("Redis 연결 성공 {}", pong);
    }

}
