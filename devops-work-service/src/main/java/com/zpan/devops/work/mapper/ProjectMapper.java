package com.zpan.devops.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.work.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
