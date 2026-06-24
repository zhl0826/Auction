package com.example.scheduler;

import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionScheduler {
    @Autowired private GoodsService goodsService;

    // 每 30 秒扫一次过期拍卖
    @Scheduled(fixedDelay = 30000L, initialDelay = 10000L)
    public void checkExpiredAuctions() {
        try {
            int n = goodsService.checkAllExpired();
            if (n > 0) {
                System.out.println("[AuctionScheduler] auto-closed " + n + " expired auction(s)");
            }
        } catch (Exception e) {
            System.err.println("[AuctionScheduler] error: " + e.getMessage());
        }
    }
}
