package com.study.redis.prac.product;

//public record ProductResponse(
//        Long id,
//        String name,
//        int price,
//        String description
//) {
//
//    public static ProductResponse from(Product product) {
//        return new ProductResponse(
//                product.getId(), product.getName(), product.getPrice(), product.getDescription()
//        );
//    }
//}

import java.io.Serializable;

public record ProductResponse(
        Long id,
        String name,
        int price,
        String description
) implements Serializable {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        );
    }
}
