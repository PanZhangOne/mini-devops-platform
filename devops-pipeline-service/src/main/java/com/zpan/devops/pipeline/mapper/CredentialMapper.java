package com.zpan.devops.pipeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.pipeline.entity.Credential;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CredentialMapper extends BaseMapper<Credential> {
}
