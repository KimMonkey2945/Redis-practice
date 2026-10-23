package com.study.redis.prac.product;

import com.study.redis.prac.common.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheableService {

    private final PracProductRepository productRepository;

//    /**
//     * 캐시에 있으면 → 이 메서드는 아예 실행 안 됨, 캐시 값 바로 반환
//     * 캐시에 없으면 → 메서드 실행 → 반환값을 캐시에 저장
//     */
//    @Cacheable(cacheNames = "prac:product-v2", key = "#productId")
//    @Transactional(readOnly = true)
//    public ProductResponse getProduct(Long productId) {
//        log.info("[메서드 실행됨 = CACHE MISS] productId={}", productId);
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));
//
//        return ProductResponse.from(product);
//    }
//
//    /**
//     * 메서드 실행 후 → 해당 캐시 삭제
//     */
//    @CacheEvict(cacheNames = "prac:product-v2", key = "#productId")
//    @Transactional
//    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
//        log.info("[수정 + CACHE EVICT] productId={}", productId);
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));
//
//        product.update(request.name(), request.price(), request.description());
//
//        return ProductResponse.from(product);
//    }

    @Cacheable(cacheNames = CacheNames.PRODUCT, key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        log.info("[메서드 실행됨 = CACHE MISS] productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));

        return ProductResponse.from(product);
    }

    @CacheEvict(cacheNames = CacheNames.PRODUCT, key = "#productId")
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("[수정 + CACHE EVICT] productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다. id=" + productId));

        product.update(request.name(), request.price(), request.description());

        return ProductResponse.from(product);
    }
}