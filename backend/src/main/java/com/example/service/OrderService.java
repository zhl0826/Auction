package com.example.service;
import com.example.entity.Order;
import java.util.List;
public interface OrderService {
    int create(Order order);
    List<Order> listByBuyer(Long buyerId);
    List<Order> listBySeller(Long sellerId);
    Order findById(Long id);
    Order findByGoodsIdAndBuyer(Long goodsId, Long buyerId);
    List<Order> listByStatus(String status);
}