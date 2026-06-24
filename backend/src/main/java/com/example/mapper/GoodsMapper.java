package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GoodsMapper {
    List<Goods> listByStatus(@Param("status") String status,
                             @Param("keyword") String keyword,
                             @Param("type") String type);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int updateReject(@Param("id") Long id, @Param("reason") String reason);
    int insert(Goods goods);
    List<Goods> listBySeller(@Param("sellerId") Long sellerId);
    int updatePrice(@Param("id") Long id, @Param("price") java.math.BigDecimal price);
    List<Goods> listActive(@Param("keyword") String keyword, @Param("type") String type);
    Goods findById(@Param("id") Long id);

    int updateCurrentBidder(@Param("id") Long id, @Param("bidderId") Long bidderId);
    int updateEndAt(@Param("id") Long id, @Param("endAt") java.time.LocalDateTime endAt);
    List<Goods> listByStatusIncludingRemoved(@Param("sellerId") Long sellerId, @Param("keyword") String keyword, @Param("status") String status);
    int deleteById(@Param("id") Long id);
}
