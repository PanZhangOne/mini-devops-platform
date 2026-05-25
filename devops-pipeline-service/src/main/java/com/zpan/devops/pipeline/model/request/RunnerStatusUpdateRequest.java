package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerStatusUpdateRequest {

    @NotBlank(message = "Runner状态不能为空")
    private String status;
}
