package com.example.mapper;

import com.example.entity.GoodsVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GoodsVectorMapper {
    int insert(GoodsVector gv);
    int update(GoodsVector gv);
    GoodsVector findByGoodsId(@Param("goodsId") Long goodsId);
    List<GoodsVector> findAll();
    int deleteByGoodsId(@Param("goodsId") Long goodsId);
}