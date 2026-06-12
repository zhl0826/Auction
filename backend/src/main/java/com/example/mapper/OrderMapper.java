package com.example.mapper;
import com.example.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    List<Order> listByBuyer(@Param("buyerId") Long buyerId);
    List<Order> listBySeller(@Param("sellerId") Long sellerId);
    Order findById(@Param("id") Long id);
}