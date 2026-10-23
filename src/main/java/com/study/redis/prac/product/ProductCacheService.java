package com.study.redis.prac.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.redis.prac.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private static final Duration BASE_TTL = Duration.ofHours(1);
    private static final int JITTER_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PracProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        String key = RedisKeys.product(productId);

        // GET prac:product:1
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("[CACHE HIT] key={}", key);
            return toObject(cached);
        }

        log.info("[CACHE MISS] key={}", key);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));

        ProductResponse response = ProductResponse.from(product);

        // SET prac:product:1 "{json}" EX 3600~3900
        redisTemplate.opsForValue().set(key, toJson(response), ttlWithJitter());

        return response;
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));

        product.update(request.name(), request.price(), request.description());

        // DEL prac:product:1
        String key = RedisKeys.product(productId);
        redisTemplate.delete(key);
        log.info("[CACHE EVICT] key={}", key);

        return ProductResponse.from(product);
    }

    private Duration ttlWithJitter() {
        return BASE_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(JITTER_SECONDS));
    }

    private String toJson(ProductResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 변환 실패", e);
        }
    }

    private ProductResponse toObject(String json) {
        try {
            return objectMapper.readValue(json, ProductResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 파싱 실패", e);
        }
    }
}