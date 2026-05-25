package com.zpan.devops.release.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VersionStatusUpdateRequest {

    @NotBlank(message = "版本状态不能为空")
    private String status;
}
