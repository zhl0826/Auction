package com.example.mapper;

import com.example.entity.AfterSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AfterSaleMapper {
    List<AfterSale> list();
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
