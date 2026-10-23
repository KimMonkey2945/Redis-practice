package com.study.redis.prac.product;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prac/products-v2")
@RequiredArgsConstructor
public class ProductCacheableController {

    private final ProductCacheableService productCacheableService;

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return productCacheableService.getProduct(productId);
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable Long productId,
                                         @RequestBody ProductUpdateRequest request) {
        return productCacheableService.updateProduct(productId, request);
    }
}
