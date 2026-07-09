package com.zpan.devops.code.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "devops.git")
public class GitStorageProperties {

    /**
     * 裸仓库存储根目录
     */
    private String repositoryRoot;

    /**
     * clone 地址基础路径
     */
    private String cloneBaseUrl;

    @Data
    public static class BasicAuth {
        private String username;

        private String token;
    }
}
