package com.zpan.devops.pipeline.model.request;

import com.zpan.devops.pipeline.enums.PipelineTriggerType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunPipelineRequest {

    @NotNull(message = "触发类型不能为空")
    private PipelineTriggerType triggerType;

    private Long triggerBy;

    private String triggerByName;

    private String branch;

    private String commitId;

    private String remark;
}
