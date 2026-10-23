package com.study.redis.prac.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PracProductRepository extends JpaRepository<Product, Long> {
}
