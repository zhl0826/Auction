package com.example.service;

import com.example.entity.Goods;
import java.util.List;

public interface GoodsService {
    List<Goods> listByStatus(String status, String keyword, String type);
    void approve(Long id);
    void reject(Long id, String reason);
    void offShelf(Long id);
    void offShelf(Long id, boolean refundBids);
    void relist(Long id);
    void delete(Long id);
    Goods detail(Long id);
    Goods add(Goods goods);
    List<Goods> listActive(String keyword, String type);
    List<Goods> listBySeller(Long sellerId);
    List<Goods> listBySellerAndStatus(Long sellerId, String status, String keyword);
    Goods placeBid(Long goodsId, Long userId, java.math.BigDecimal price);
    void cancelBid(Long bidId, Long userId);
    java.util.List<com.example.entity.Bid> listBids(Long goodsId);
    void closeAuction(Long goodsId, Long userId);
    void finishExpired(Long goodsId);
    int checkAllExpired();
    List<Goods> listSoldByType(String type, Integer limit);
}