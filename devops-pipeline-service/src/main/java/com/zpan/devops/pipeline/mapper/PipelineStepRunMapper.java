package com.zpan.devops.pipeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.pipeline.entity.PipelineStepRun;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineStepRunMapper extends BaseMapper<PipelineStepRun> {
}
