package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CredentialUpdateRequest {
    @NotBlank(message = "凭据名称不能为空")
    @Size(max = 100, message = "凭据名称长度不能超过 100")
    private String name;

    @NotBlank(message = "凭据类型不能为空")
    @Size(max = 50, message = "凭据类型长度不能超过 50")
    private String credentialType;

    @Size(max = 200, message = "用户名长度不能超过 200")
    private String username;

    /**
     * 更新时可以为空。
     * 为空表示不修改原 secretValue。
     */
    private String secretValue;

    @Size(max = 500, message = "凭据描述长度不能超过 500")
    private String description;
}
