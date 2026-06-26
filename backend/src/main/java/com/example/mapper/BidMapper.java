package com.example.mapper;

import com.example.entity.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BidMapper {
    int insert(Bid bid);
    List<Bid> listByBidder(@Param("bidderId") Long bidderId);
    List<Bid> listByGoods(@Param("goodsId") Long goodsId);
    Bid findById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int updateRefunded(@Param("id") Long id, @Param("refunded") boolean refunded);
}
