package com.example.mapper;

import com.example.entity.AuctionAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuctionAdminMapper {
    List<AuctionAdmin> list(@Param("keyword") String keyword);
    int insert(AuctionAdmin a);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int delete(@Param("id") Long id);
    AuctionAdmin findByUsername(@Param("username") String username);
}
