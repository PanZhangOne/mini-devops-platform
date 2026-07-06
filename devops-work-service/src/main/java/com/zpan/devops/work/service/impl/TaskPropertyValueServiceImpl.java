package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.domain.TaskPropertyValueValidator;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.entity.TaskProperty;
import com.zpan.devops.work.entity.TaskPropertyValue;
import com.zpan.devops.work.enums.TaskPropertyType;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.mapper.TaskPropertyMapper;
import com.zpan.devops.work.mapper.TaskPropertyValueMapper;
import com.zpan.devops.work.model.request.TaskPropertyValueSaveItemRequest;
import com.zpan.devops.work.model.request.TaskPropertyValueSaveRequest;
import com.zpan.devops.work.model.vo.TaskPropertyValueVO;
import com.zpan.devops.work.service.TaskPropertyValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskPropertyValueServiceImpl implements TaskPropertyValueService {

    private final TaskMapper taskMapper;

    private final TaskPropertyMapper taskPropertyMapper;

    private final TaskPropertyValueMapper taskPropertyValueMapper;

    private final TaskPropertyValueValidator taskPropertyValueValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveValues(Long taskId, TaskPropertyValueSaveRequest request) {
        Task task = getTaskOrThrow(taskId);
        LocalDateTime now = LocalDateTime.now();

        for (TaskPropertyValueSaveItemRequest item : request.getValues()) {
            TaskProperty property = getTaskPropertyOrThrow(item.getPropertyId());
            if (!task.getProjectId().equals(property.getProjectId())) {
                throw new BizException(ErrorCode.TASK_PROPERTY_NOT_FOUND);
            }
            if (!Boolean.TRUE.equals(property.getEnabled())) {
                throw new BizException(ErrorCode.TASK_PROPERTY_DISABLED);
            }

            taskPropertyValueValidator.validate(property, item.getValueText());
            saveOrUpdateValue(taskId, property, item.getValueText(), now);
        }
    }

    @Override
    public List<TaskPropertyValueVO> listByTaskId(Long taskId) {
        Task task = getTaskOrThrow(taskId);

        LambdaQueryWrapper<TaskProperty> propertyWrapper = new LambdaQueryWrapper<>();
        propertyWrapper.eq(TaskProperty::getProjectId, task.getProjectId());
        propertyWrapper.eq(TaskProperty::getEnabled, Boolean.TRUE);
        propertyWrapper.orderByAsc(TaskProperty::getSortOrder);
        propertyWrapper.orderByAsc(TaskProperty::getId);
        List<TaskProperty> properties = taskPropertyMapper.selectList(propertyWrapper);

        LambdaQueryWrapper<TaskPropertyValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(TaskPropertyValue::getTaskId, taskId);
        Map<Long, TaskPropertyValue> valueMap = taskPropertyValueMapper
                .selectList(valueWrapper).stream().collect(Collectors.toMap(TaskPropertyValue::getPropertyId, item -> item));

        return properties.stream().map(property -> toVO(property, valueMap.get(property.getId()), taskId)).toList();
    }

    private void saveOrUpdateValue(Long taskId, TaskProperty property, String valueText, LocalDateTime now) {
        TaskPropertyValue taskPropertyValue = new TaskPropertyValue();
        taskPropertyValue.setTaskId(taskId);
        taskPropertyValue.setPropertyId(property.getId());
        taskPropertyValue.setPropertyCode(property.getCode());
        taskPropertyValue.setValueText(valueText);
        taskPropertyValue.setUpdatedAt(now);
        taskPropertyValue.setCreatedAt(now);
        taskPropertyValueMapper.insertOrUpdate(taskPropertyValue);
    }

    private Task getTaskOrThrow(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private TaskProperty getTaskPropertyOrThrow(Long id) {
        TaskProperty property = taskPropertyMapper.selectById(id);
        if (property == null) {
            throw new BizException(ErrorCode.TASK_PROPERTY_NOT_FOUND);
        }
        return property;
    }

    private TaskPropertyValueVO toVO(TaskProperty property, TaskPropertyValue value, Long taskId) {
        TaskPropertyValueVO vo = new TaskPropertyValueVO();
        vo.setId(value == null ? null : value.getId());
        vo.setTaskId(taskId);
        vo.setPropertyId(property.getId());
        vo.setPropertyCode(property.getCode());
        vo.setPropertyName(property.getName());
        vo.setPropertyType(property.getPropertyType());
        vo.setRequired(property.getRequired());
        vo.setOptionsJson(property.getOptionsJson());
        vo.setValueText(value == null ? null : value.getValueText());
        vo.setCreatedAt(value == null ? null : value.getCreatedAt());
        vo.setUpdatedAt(value == null ? null : value.getUpdatedAt());

        if (TaskPropertyType.isValid(property.getPropertyType())) {
            vo.setPropertyTypeDescription(
                    TaskPropertyType.valueOf(property.getPropertyType()).getDescription()
            );
        }

        return vo;
    }
}
