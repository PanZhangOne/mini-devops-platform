package com.zpan.devops.release.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VersionUpdateRequest {

    @Size(max = 100, message = "Git Tag 长度不能超过 100")
    private String gitTag;

    @Size(max = 100, message = "分支名称长度不能超过 100")
    private String branchName;

    @Size(max = 100, message = "提交哈希长度不能超过 100")
    private String commitHash;

    @NotBlank(message = "版本标题不能为空")
    @Size(max = 200, message = "版本标题长度不能超过 200")
    private String title;

    private String status;

    private String description;
}
