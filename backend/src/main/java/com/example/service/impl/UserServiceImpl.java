package com.example.service.impl;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.BillService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserMapper userMapper;
    @Autowired private BillService billService;

    @Override
    public Map<String, Object> page(String keyword, String role, String status, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        List<User> all = userMapper.list(keyword, role, status);
        int total = all.size();
        List<User> rows = all.subList(offset, Math.min(offset + size, total));
        Map<String, Object> data = new HashMap<>();
        data.put("list", rows);
        data.put("total", total);
        return data;
    }

    @Override
    public void updateStatus(Long id, String status) {
        userMapper.updateStatus(id, status);
    }

    @Override
    public User register(String username, String password, String nickname, String phone) {
        User exist = userMapper.findByUsername(username);
        if (exist != null) throw new RuntimeException("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setNickname(nickname);
        u.setBalance(java.math.BigDecimal.ZERO);
        u.setStatus("active");
        userMapper.insert(u);
        return u;
    }

    
    /** 充值: 加余额 + 写流水 */
    @Override
    @Transactional
    public User recharge(Long userId, java.math.BigDecimal amount) {
        User u = userMapper.findById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        java.math.BigDecimal newBalance = u.getBalance().add(amount);
        userMapper.updateBalance(userId, newBalance);
        billService.record(userId, "recharge", amount, "recharge", null, "充值");
        return userMapper.findById(userId);
    }

    @Override
    public User updateBalance(Long id, java.math.BigDecimal balance) {
        userMapper.updateBalance(id, balance);
        return userMapper.findById(id);
    }

    @Override
    public User getById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User login(String username, String password) {
        User u = userMapper.findByUsername(username);
        if (u == null) throw new RuntimeException("账号不存在");
        if (!u.getPassword().equals(password)) throw new RuntimeException("账号或密码错误");
        if (!"active".equals(u.getStatus())) throw new RuntimeException("账号已被封禁，请联系管理员");
        return u;
    }
}