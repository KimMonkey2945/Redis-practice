package com.study.redis.stock;

import com.study.redis.stock.jpa.StockJpaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockLockFacade stockLockFacade;
    private final StockJpaService stockJpaService;

    @PostMapping("/jpa/stock/reduce/{itemId}")
    public Map<String, Object> reduceWithJpa(@PathVariable Long itemId,
                                             @RequestParam(defaultValue = "unknown") String userId) {
        int remainQty = stockLockFacade.decreaseWithJpa(itemId, userId);
        return Map.of("message", "Item " + itemId + " stock reduced successfully for " + userId, "remain_qty", remainQty);
    }

//    @PostMapping("/mybatis/stock/reduce/{itemId}")
//    public Map<String, Object> reduceWithMybatis(@PathVariable Long itemId,
//                                                 @RequestParam(defaultValue = "unknown") String userId) {
//        int remainQty = stockLockFacade.decreaseWithMybatis(itemId, userId);
//        return Map.of("message", "Item " + itemId + " stock reduced successfully for " + userId, "remain_qty", remainQty);
//    }

    /** (비교 실험용) 락 없이 바로 차감 */
    @PostMapping("/jpa/stock/reduce-unsafe/{itemId}")
    public Map<String, Object> reduceWithoutLock(@PathVariable Long itemId,
                                                 @RequestParam(defaultValue = "unknown") String userId) {
        int remainQty = stockJpaService.decreaseStock(itemId, userId);
        return Map.of("message", "Item " + itemId + " stock reduced (no lock) for " + userId, "remain_qty", remainQty);
    }
}