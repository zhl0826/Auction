package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    List<User> list(@Param("keyword") String keyword,
                    @Param("role") String role,
                    @Param("status") String status);
    int count(@Param("keyword") String keyword,
              @Param("role") String role,
              @Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int insert(User user);
    int updateBalance(@Param("id") Long id, @Param("balance") java.math.BigDecimal balance);
    User findById(@Param("id") Long id);
    User findByUsername(@Param("username") String username);
}
