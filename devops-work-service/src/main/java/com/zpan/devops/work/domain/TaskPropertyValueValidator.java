package com.zpan.devops.work.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.TaskProperty;
import com.zpan.devops.work.enums.TaskPropertyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class TaskPropertyValueValidator {

    private final ObjectMapper objectMapper;

    public void validate(TaskProperty property, String valueText) {
        if (Boolean.TRUE.equals(property.getRequired()) && (valueText == null || valueText.isBlank())) {
            throw new BizException(ErrorCode.TASK_PROPERTY_REQUIRED, property.getName() + " 为必填项目");
        }
        if (valueText == null || valueText.isBlank()) {
            return;
        }

        String type = property.getPropertyType();
        if (TaskPropertyType.TEXT.name().equals(type)) {
            return;
        }
        if (TaskPropertyType.NUMBER.name().equals(type)) {
            validateNumber(property, valueText);
            return;
        }
        if (TaskPropertyType.DATE.name().equals(type)) {
            validateDate(property, valueText);
            return;
        }
        if (TaskPropertyType.SELECT.name().equals(type)) {
            validateSelect(property, valueText);
            return;
        }
        if (TaskPropertyType.BOOLEAN.name().equals(type)) {
            validateBoolean(valueText);
            return;
        }
        if (TaskPropertyType.USER.name().equals(type)) {
            validateLong(valueText);
            return;
        }
        if (TaskPropertyType.MULTI_SELECT.name().equals(type)) {
            validateMultiSelect(property, valueText);
        }
    }

    private void validateNumber(TaskProperty property, String valueText) {
        try {
            new BigDecimal(valueText);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, property.getName() + " 值必须为数字");
        }
    }

    private void validateDate(TaskProperty property, String valueText) {
        try {
            LocalDateTime.parse(valueText);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, property.getName() + " 值必须为日期格式");
        }
    }

    private void validateSelect(TaskProperty property, String valueText) {
        if (!isOptionValueExists(property.getOptionsJson(), valueText) && !"false".equalsIgnoreCase(valueText)) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "选项值不存在: " + valueText);
        }
    }

    private void validateBoolean(String valueText) {
        if (!"true".equalsIgnoreCase(valueText) && !"false".equalsIgnoreCase(valueText)) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "布尔值必须为 true 或 false");
        }
    }

    private void validateLong(String valueText) {
        try {
            Long.parseLong(valueText);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "用户属性值必须是用户ID");
        }
    }

    private void validateMultiSelect(TaskProperty property, String valueText) {
        try {
            JsonNode node = objectMapper.readTree(valueText);
            if (!node.isArray()) {
                throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "多选属性值必须是 JSON 数组");
            }

            for (JsonNode item : node) {
                if (!item.isTextual()) {
                    throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "多选属性值必须是字符串数组");
                }

                String value = item.asText();
                if (!isOptionValueExists(property.getOptionsJson(), value)) {
                    throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "选项值不存在:" + value);
                }
            }

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.TASK_PROPERTY_VALUE_INVALID, "多选属性值必须是 JSON 数组");
        }
    }

    private boolean isOptionValueExists(String optionJson, String value) {
        if (optionJson == null || optionJson.isBlank()) {
            return false;
        }

        try {
            JsonNode node = objectMapper.readTree(optionJson);
            // 验证json格式
            if (!node.isArray()) {
                return false;
            }
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                JsonNode item = elements.next();
                JsonNode valueNode = item.get("value");

                if (valueNode == null || value.equals(valueNode.asText())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
