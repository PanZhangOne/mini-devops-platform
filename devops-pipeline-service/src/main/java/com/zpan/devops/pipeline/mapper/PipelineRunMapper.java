package com.zpan.devops.pipeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.pipeline.entity.PipelineRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PipelineRunMapper extends BaseMapper<PipelineRun> {
    int claimPendingRun(
            @Param("id") Long id,
            @Param("expectedStatus") String expectedStatus,
            @Param("newStatus") String newStatus,
            @Param("runnerName") String runnerName,
            @Param("startedAt") LocalDateTime startedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
