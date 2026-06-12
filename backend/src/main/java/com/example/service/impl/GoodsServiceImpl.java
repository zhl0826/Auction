package com.example.service.impl;

import com.example.entity.Goods;
import com.example.entity.Bid;
import com.example.entity.Order;
import com.example.entity.User;
import com.example.mapper.GoodsMapper;
import com.example.mapper.BidMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.OrderMapper;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired private GoodsMapper mapper;
    @Autowired private BidMapper bidMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;

    @Override public List<Goods> listByStatus(String status, String keyword, String type) {
        return mapper.listByStatus(status, keyword, type);
    }
    @Override public void approve(Long id) { mapper.updateStatus(id, "on_sale"); }
    @Override public void reject(Long id, String reason) { mapper.updateReject(id, reason); }
    @Override public void offShelf(Long id) { mapper.updateStatus(id, "off_shelf"); }
    @Override public Goods detail(Long id) { return mapper.findById(id); }
    @Override
    public List<Goods> listActive(String keyword, String type) {
        return mapper.listActive(keyword, type);
    }
    @Override
    public Goods add(Goods goods) {
        goods.setCurrentPrice(goods.getStartPrice());
        goods.setStatus("pending");
        mapper.insert(goods);
        return goods;
    }
    @Override
    public List<Goods> listBySeller(Long sellerId) {
        return mapper.listBySeller(sellerId);
    }
    @Override
    @Transactional
    public Goods placeBid(Long goodsId, Long userId, BigDecimal price) {
        Goods g = mapper.findById(goodsId);
        if (g == null) throw new RuntimeException("商品不存在");
        if (!"on_sale".equals(g.getStatus())) throw new RuntimeException("商品不在竞拍中");
        if (g.getEndAt() != null && g.getEndAt().isBefore(LocalDateTime.now())) throw new RuntimeException("竞拍已结束");
        if (g.getSellerId().equals(userId)) throw new RuntimeException("不能竞拍自己的商品");
        if (price.compareTo(g.getCurrentPrice()) <= 0) throw new RuntimeException("出价必须高于当前最高价");
        User bidder = userMapper.findById(userId);
        if (bidder == null) throw new RuntimeException("用户不存在");
        if (bidder.getBalance().compareTo(price) < 0) throw new RuntimeException("余额不足");
        userMapper.updateBalance(userId, bidder.getBalance().subtract(price));
        User seller = userMapper.findById(g.getSellerId());
        if (seller != null) userMapper.updateBalance(seller.getId(), seller.getBalance().add(price));
        mapper.updatePrice(goodsId, price);
        Bid bid = new Bid();
        bid.setGoodsId(goodsId);
        bid.setBidderId(userId);
        bid.setPrice(price);
        bidMapper.insert(bid);
        Order order = new Order();
        order.setGoodsId(goodsId);
        order.setGoodsTitle(g.getTitle());
        order.setBuyerId(userId);
        order.setSellerId(g.getSellerId());
        order.setAmount(price);
        order.setStatus("completed");
        orderMapper.insert(order);
        return mapper.findById(goodsId);
    }

}
