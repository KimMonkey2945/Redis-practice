package com.study.redis.prac.product;

public record ProductUpdateRequest( String name,
                                    int price,
                                    String description) {

}
