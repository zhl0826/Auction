package com.example.service;

import com.example.entity.Goods;
import java.util.List;

public interface GoodsService {
    List<Goods> listByStatus(String status, String keyword, String type);
    void approve(Long id);
    void reject(Long id, String reason);
    void offShelf(Long id);
    Goods detail(Long id);
    Goods add(Goods goods);
    List<Goods> listActive(String keyword, String type);
    List<Goods> listBySeller(Long sellerId);
    Goods placeBid(Long goodsId, Long userId, java.math.BigDecimal price);
}
