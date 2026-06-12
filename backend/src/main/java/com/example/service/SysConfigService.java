package com.example.service;

import com.example.entity.SysConfig;

public interface SysConfigService {
    SysConfig get();
    void save(SysConfig cfg);
}
