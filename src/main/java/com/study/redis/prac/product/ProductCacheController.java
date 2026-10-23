package com.study.redis.prac.product;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prac/products")
@RequiredArgsConstructor
public class ProductCacheController {

    private final ProductCacheService productCacheService;

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return productCacheService.getProduct(productId);
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable Long productId,
                                         @RequestBody ProductUpdateRequest request) {
        return productCacheService.updateProduct(productId, request);
    }
}
