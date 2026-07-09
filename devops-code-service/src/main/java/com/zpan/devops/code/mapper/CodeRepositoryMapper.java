package com.zpan.devops.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.code.entity.CodeRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodeRepositoryMapper extends BaseMapper<CodeRepository> {
}
