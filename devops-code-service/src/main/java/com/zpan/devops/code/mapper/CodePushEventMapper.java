package com.zpan.devops.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.code.entity.CodeCommit;
import com.zpan.devops.code.entity.CodePushEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodePushEventMapper extends BaseMapper<CodePushEvent> {
}
