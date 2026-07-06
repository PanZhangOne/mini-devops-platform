package com.zpan.devops.work.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TaskPropertyValueSaveRequest {
    @Valid
    @NotEmpty(message = "属性值列表不能为空")
    private List<TaskPropertyValueSaveItemRequest> values;
}
