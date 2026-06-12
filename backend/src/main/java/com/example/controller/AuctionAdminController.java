package com.example.controller;

import com.example.common.Result;
import com.example.entity.AfterSale;
import com.example.entity.Bid;
import com.example.entity.Goods;
import com.example.service.AfterSaleService;
import com.example.service.BidService;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auction")
public class AuctionAdminController {

    @Autowired private GoodsService goodsService;
    @Autowired private BidService bidService;
    @Autowired private AfterSaleService afterSaleService;

    // ========== 商品审核 ==========
    @GetMapping("/goods/pending")
    public Result<List<Goods>> pending(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String type) {
        return Result.ok(goodsService.listByStatus("pending", keyword, type));
    }

    @PutMapping("/goods/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        goodsService.approve(id);
        return Result.ok();
    }

    @PutMapping("/goods/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        goodsService.reject(id, body.get("reason"));
        return Result.ok();
    }

    // ========== 已上架商品（自由下架） ==========
    @GetMapping("/goods/onsale")
    public Result<List<Goods>> onSale(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String type) {
        return Result.ok(goodsService.listByStatus("on_sale", keyword, type));
    }

    @PutMapping("/goods/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        goodsService.offShelf(id);
        return Result.ok();
    }

    // ========== 竞拍历史（按商品） ==========
    @GetMapping("/goods/{id}/bids")
    public Result<List<Bid>> bids(@PathVariable Long id) {
        Goods g = goodsService.detail(id);
        if (g == null) return Result.error(404, "商品不存在");
        return Result.ok(bidService.listByGoods(id));
    }

    // ========== 售后处理 ==========
    @GetMapping("/aftersales")
    public Result<List<AfterSale>> aftersales() { return Result.ok(afterSaleService.list()); }

    @PutMapping("/aftersales/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) { afterSaleService.refund(id); return Result.ok(); }

    @PutMapping("/aftersales/{id}/reject")
    public Result<Void> rejectAfterSale(@PathVariable Long id) { afterSaleService.reject(id); return Result.ok(); }
}
