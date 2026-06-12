package com.example.service.impl;

import com.example.entity.AuctionAdmin;
import com.example.mapper.AuctionAdminMapper;
import com.example.service.AuctionAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuctionAdminServiceImpl implements AuctionAdminService {
    @Autowired private AuctionAdminMapper mapper;

    @Override public List<AuctionAdmin> list(String keyword) { return mapper.list(keyword); }

    @Override
    public void add(AuctionAdmin a) {
        if (mapper.findByUsername(a.getUsername()) != null) {
            throw new RuntimeException("账号已存在");
        }
        a.setStatus("active");
        mapper.insert(a);
    }

    @Override public void updateStatus(Long id, String status) { mapper.updateStatus(id, status); }
    @Override public void delete(Long id) { mapper.delete(id); }

    @Override
    public AuctionAdmin login(String username, String password) {
        AuctionAdmin a = mapper.findByUsername(username);
        if (a == null) throw new RuntimeException("账号不存在");
        if (!a.getPassword().equals(password)) throw new RuntimeException("账号或密码错误");
        if (!"active".equals(a.getStatus())) throw new RuntimeException("账号已停用，请联系系统管理员");
        return a;
    }
}
