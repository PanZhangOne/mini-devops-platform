package com.zpan.devops.release.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.release.entity.PipelineRun;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineRunMapper extends BaseMapper<PipelineRun> {
}
