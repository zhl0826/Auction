package com.example.service.impl;

import com.example.entity.Bid;
import com.example.mapper.BidMapper;
import com.example.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BidServiceImpl implements BidService {
    @Autowired private BidMapper mapper;
    @Override public List<Bid> listByGoods(Long goodsId) { return mapper.listByGoods(goodsId); }
    @Override public List<Bid> listByUserId(Long userId) { return mapper.listByBidder(userId); }
}
