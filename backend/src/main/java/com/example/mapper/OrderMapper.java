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
    Order findByGoodsIdAndBuyer(@Param("goodsId") Long goodsId, @Param("buyerId") Long buyerId);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int updateAfterSaleStatus(@Param("id") Long id, @Param("status") String status);
    List<Order> listAll(@Param("status") String status);
}