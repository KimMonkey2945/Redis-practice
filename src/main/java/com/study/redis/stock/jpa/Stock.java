package com.study.redis.stock.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    private Long itemId;

    private String itemName;
    private Integer stockQty;

    public void decrease() {
        if (this.stockQty <= 0) {
            throw new IllegalStateException("재고가 없습니다.");
        }
        this.stockQty = this.stockQty - 1;
    }
}
