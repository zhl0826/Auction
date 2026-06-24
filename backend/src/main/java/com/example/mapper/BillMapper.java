package com.example.mapper;

import com.example.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BillMapper {
    int insert(Bill bill);
    List<Bill> listByUser(@Param("userId") Long userId,
                          @Param("type") String type,
                          @Param("limit") Integer limit);
}
