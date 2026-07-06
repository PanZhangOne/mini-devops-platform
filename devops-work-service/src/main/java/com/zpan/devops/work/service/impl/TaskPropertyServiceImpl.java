package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.TaskProperty;
import com.zpan.devops.work.enums.TaskPropertyType;
import com.zpan.devops.work.mapper.ProjectMapper;
import com.zpan.devops.work.mapper.TaskPropertyMapper;
import com.zpan.devops.work.mapper.TaskPropertyValueMapper;
import com.zpan.devops.work.model.request.TaskPropertyCreateRequest;
import com.zpan.devops.work.model.request.TaskPropertyUpdateRequest;
import com.zpan.devops.work.model.vo.TaskPropertyVO;
import com.zpan.devops.work.service.ProjectService;
import com.zpan.devops.work.service.TaskPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskPropertyServiceImpl implements TaskPropertyService {

    private final ProjectService projectService;

    private final TaskPropertyMapper taskPropertyMapper;


    @Override
    public TaskPropertyVO create(Long projectId, TaskPropertyCreateRequest request, Long currentUserId) {
        validateProjectExists(projectId);
        validatePropertyType(request.getPropertyType());
        validateCodeNotExists(projectId, request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        TaskProperty property = new TaskProperty();
        property.setCode(request.getCode());
        property.setProjectId(projectId);
        property.setName(request.getName());
        property.setCode(request.getCode());
        property.setPropertyType(request.getPropertyType());
        property.setRequired(request.getRequired() != null && request.getRequired());
        property.setOptionsJson(request.getOptionsJson());
        property.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        property.setEnabled(request.getEnabled() == null || request.getEnabled());
        property.setCreatedBy(currentUserId);
        property.setCreatedAt(now);
        property.setUpdatedAt(now);

        taskPropertyMapper.insert(property);
        return toVO(property);
    }

    @Override
    public List<TaskPropertyVO> listByProjectId(Long projectId) {
        LambdaQueryWrapper<TaskProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskProperty::getProjectId, projectId);
        wrapper.orderByAsc(TaskProperty::getSortOrder);
        wrapper.orderByAsc(TaskProperty::getId);

        return taskPropertyMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public TaskPropertyVO getById(Long id) {
        return toVO(findOrThrow(id));
    }

    @Override
    public TaskPropertyVO update(Long id, TaskPropertyUpdateRequest request) {
        TaskProperty property = findOrThrow(id);

        validatePropertyType(request.getPropertyType());
        validateCodeNotExists(property.getProjectId(), request.getCode(), id);

        property.setName(request.getName());
        property.setCode(request.getCode());
        property.setPropertyType(request.getPropertyType());
        property.setRequired(request.getRequired() != null && request.getRequired());
        property.setOptionsJson(request.getOptionsJson());
        property.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        property.setEnabled(request.getEnabled() == null || request.getEnabled());
        property.setUpdatedAt(LocalDateTime.now());

        taskPropertyMapper.updateById(property);
        return toVO(property);
    }

    @Override
    public void delete(Long id) {
        TaskProperty property = findOrThrow(id);
        taskPropertyMapper.deleteById(property);
    }

    private void validateProjectExists(Long projectId) {
        projectService.getById(projectId);
    }

    private void validatePropertyType(String type) {
        if (!TaskPropertyType.isValid(type)) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID);
        }
    }

    private void validateCodeNotExists(Long projectId, String code, Long excludeId) {
        LambdaQueryWrapper<TaskProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskProperty::getProjectId, projectId)
                .eq(TaskProperty::getCode, code);
        if (excludeId != null) {
            wrapper.ne(TaskProperty::getId, excludeId);
        }

        Long count = taskPropertyMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.TASK_PROPERTY_CODE_EXISTS);
        }
    }

    private TaskProperty findOrThrow(Long id) {
        TaskProperty property = taskPropertyMapper.selectById(id);
        if (property == null) {
            throw new BizException(ErrorCode.TASK_PROPERTY_NOT_FOUND);
        }
        return property;
    }

    private TaskPropertyVO toVO(TaskProperty property) {
        TaskPropertyVO vo = new TaskPropertyVO();
        vo.setId(property.getId());
        vo.setProjectId(property.getProjectId());
        vo.setCode(property.getCode());
        vo.setName(property.getName());
        vo.setPropertyType(property.getPropertyType());
        vo.setRequired(property.getRequired());
        vo.setOptionsJson(property.getOptionsJson());
        vo.setSortOrder(property.getSortOrder());
        vo.setEnabled(property.getEnabled());
        vo.setCreatedAt(property.getCreatedAt());
        vo.setUpdatedAt(property.getUpdatedAt());

        if (property.getPropertyType() != null && TaskPropertyType.isValid(property.getPropertyType())) {
            vo.setPropertyTypeDescription(TaskPropertyType.valueOf(property.getPropertyType()).getDescription());
        }

        return vo;
    }
}
