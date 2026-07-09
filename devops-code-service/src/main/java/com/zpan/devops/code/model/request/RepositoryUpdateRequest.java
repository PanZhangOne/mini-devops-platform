package com.zpan.devops.code.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepositoryUpdateRequest {

    @Size(max = 100, message = "仓库名称长度不能超过 100")
    private String name;

    @Size(max = 500, message = "仓库描述长度不能超过 500")
    private String description;

    /**
     * PRIVATE / INTERNAL / PUBLIC
     */
    private String visibility;
}