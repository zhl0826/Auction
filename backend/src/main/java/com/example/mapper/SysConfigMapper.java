package com.example.mapper;

import com.example.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysConfigMapper {
    SysConfig get();
    int update(SysConfig cfg);
}
