package com.zpan.devops.pipeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.pipeline.entity.Pipeline;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineMapper extends BaseMapper<Pipeline> {
}
