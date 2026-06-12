package com.example.service;

import com.example.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> page(String keyword, String role, String status, int page, int size);
    void updateStatus(Long id, String status);
    User login(String username, String password);
    User register(String username, String password, String nickname, String phone);
    User updateBalance(Long id, java.math.BigDecimal balance);
    User getById(Long id);
}
