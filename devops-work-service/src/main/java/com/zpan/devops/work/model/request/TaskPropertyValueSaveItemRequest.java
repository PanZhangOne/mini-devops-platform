package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskPropertyValueSaveItemRequest {
    @NotNull(message = "属性ID不能为空")
    private Long propertyId;

    private String valueText;
}
