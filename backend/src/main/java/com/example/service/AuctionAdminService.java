package com.example.service;

import com.example.entity.AuctionAdmin;
import java.util.List;

public interface AuctionAdminService {
    List<AuctionAdmin> list(String keyword);
    void add(AuctionAdmin a);
    void updateStatus(Long id, String status);
    void delete(Long id);
    AuctionAdmin login(String username, String password);
}
