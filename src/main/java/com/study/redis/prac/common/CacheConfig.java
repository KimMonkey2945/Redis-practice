package com.study.redis.prac.common;

import com.study.redis.prac.product.ProductResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // 1. 모든 캐시에 공통으로 적용할 기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 키는 문자열 그대로 저장
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 키 모양: "캐시이름::id" → "캐시이름:id"
                .computePrefixWith(cacheName -> cacheName + ":")
                // 기본 TTL 30분
                .entryTtl(Duration.ofMinutes(30))
                // null은 캐시하지 않음
                .disableCachingNullValues();

        // 2. 상품 캐시 전용 설정: 값은 ProductResponse JSON, TTL 1시간
        RedisCacheConfiguration productConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(ProductResponse.class)))
                .entryTtl(Duration.ofHours(1));

        // 3. 캐시 매니저 생성
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CacheNames.PRODUCT, productConfig)
                .build();
    }

}
