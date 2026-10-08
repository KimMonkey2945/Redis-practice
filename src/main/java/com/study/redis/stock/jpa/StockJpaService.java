package com.study.redis.stock.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockJpaService {

    private final StockRepository stockRepository;

    @Transactional
    public int decreaseStock(Long itemId, String userId) {
        Stock stock = stockRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (stock.getStockQty() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "품절되었습니다. (요청자: " + userId + ")");
        }
        // 어떤 유저의 요청이 처리 중인지 로그로 확인
        log.info("📦 [Success] Item {} 재고 차감 작업 중... 현재 재고 {} (요청자: {})", itemId, stock.getStockQty(), userId);
        sleep(2000);   // 무거운 비즈니스 로직 시뮬레이션

        stock.decrease();   // 변경 감지 → 커밋 시 UPDATE
        return stock.getStockQty();
    }




    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}
