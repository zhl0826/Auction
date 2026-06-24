package com.example.service;

import com.example.entity.Bid;
import java.util.List;

public interface BidService {
    List<Bid> listByGoods(Long goodsId);
    List<Bid> listByUserId(Long userId);
}
