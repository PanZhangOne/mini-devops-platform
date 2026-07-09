package com.zpan.devops.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpan.devops.code.entity.CodeRepositoryMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodeRepositoryMemberMapper extends BaseMapper<CodeRepositoryMember> {
}
