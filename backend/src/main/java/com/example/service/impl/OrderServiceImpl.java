package com.example.service.impl;
import com.example.entity.Order;
import com.example.mapper.OrderMapper;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired private OrderMapper mapper;
    @Override public int create(Order order) { return mapper.insert(order); }
    @Override public List<Order> listByBuyer(Long buyerId) { return mapper.listByBuyer(buyerId); }
    @Override public List<Order> listBySeller(Long sellerId) { return mapper.listBySeller(sellerId); }
    @Override public Order findById(Long id) { return mapper.findById(id); }
    @Override public Order findByGoodsIdAndBuyer(Long goodsId, Long buyerId) { return mapper.findByGoodsIdAndBuyer(goodsId, buyerId); }
    @Override public List<Order> listByStatus(String status) { return mapper.listAll(status); }
}