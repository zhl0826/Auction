package com.example.service.impl;

import com.example.entity.SysConfig;
import com.example.mapper.SysConfigMapper;
import com.example.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl implements SysConfigService {
    @Autowired private SysConfigMapper mapper;
    @Override public SysConfig get() { return mapper.get(); }
    @Override public void save(SysConfig cfg) { mapper.update(cfg); }
}
